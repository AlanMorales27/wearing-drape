package com.wearingdrape.app.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Prenda expuesta a la UI (snapshot guardado, no catálogo vigente — FR-013). */
data class Garment(
    val id: Long,
    val name: String,
    val generalKey: String,
    val specific: String,
    val color: String,
    val fit: String?,
    val notes: String,
    val photoPath: String?,
    val createdAt: Long,
)

fun GarmentEntity.toGarment() = Garment(
    id = id,
    name = name,
    generalKey = generalKey,
    specific = specific,
    color = color,
    fit = fit,
    notes = notes,
    photoPath = photoPath,
    createdAt = createdAt,
)

/** Errores de validación por campo (contrato add-garment-flow). */
data class GarmentValidationErrors(
    val name: String? = null,
    val generalKey: String? = null,
    val specific: String? = null,
    val color: String? = null,
    val fit: String? = null,
    val notes: String? = null,
    val photo: String? = null,
) {
    val hasErrors: Boolean
        get() = listOf(name, generalKey, specific, color, fit, notes, photo).any { it != null }
}

/** Resultado de `save()`: OK con id o rechazo con errores por campo. */
sealed interface SaveResult {
    data class Ok(val id: Long) : SaveResult
    data class Rejected(val errors: GarmentValidationErrors) : SaveResult
}

/**
 * Único punto de entrada a Data (constitución II).
 *
 * Valida obligatorios + coherencia general→específico/fit contra el catálogo
 * (FR-004/FR-005/FR-006, SC-002/SC-004) y hace guardado atómico:
 * primero copia la foto, luego inserta; si el insert falla se borra el
 * fichero copiado (FR-012, sin fila parcial).
 */
class GarmentRepository(
    private val dao: GarmentDao,
    private val photos: FilePhotoDataSource,
    private val catalog: StaticCatalogDataSource = StaticCatalogDataSource(),
) {
    fun observeGarments(): Flow<List<Garment>> =
        dao.observeAll().map { list -> list.map { it.toGarment() } }

    val specificsByGeneral: Map<GarmentGeneral, List<String>> get() = catalog.specifics
    val fitsByGeneral: Map<GarmentGeneral, List<String>> get() = catalog.fits
    /** Paleta predefinida del picker de color (FR-011, solo hex). */
    val colorOptions: List<String> get() = catalog.colors

    /** Resuelve una ruta relativa a fichero absoluto (null si no existe). */
    fun photoFile(relativePath: String?): java.io.File? = photos.resolve(relativePath)

    suspend fun save(
        name: String,
        generalKey: String?,
        specific: String?,
        color: String,
        fit: String?,
        notes: String,
        photoUri: Uri?,
    ): SaveResult {
        val trimmedName = name.trim()
        val trimmedColor = color.trim()
        val trimmedSpecific = specific?.trim().orEmpty()
        val trimmedFit = fit?.trim().orEmpty()
        val trimmedNotes = notes.trim()

        val general = GarmentGeneral.fromKey(generalKey)
        val allowedSpecifics = catalog.specificsFor(general)
        val allowedFits = catalog.fitsFor(general)

        var photoError: String? = null
        var photoExt: String? = null
        if (photoUri != null) {
            when (val v = photos.validate(photoUri)) {
                is FilePhotoDataSource.Validation.Ok -> photoExt = v.ext
                is FilePhotoDataSource.Validation.Rejected -> photoError = v.message
            }
        }

        val errors = GarmentValidationErrors(
            name = when {
                trimmedName.isEmpty() -> "El nombre es obligatorio."
                trimmedName.length > 60 -> "El nombre admite hasta 60 caracteres."
                else -> null
            },
            generalKey = if (general == null) "Elige el tipo general primero." else null,
            specific = when {
                trimmedSpecific.isEmpty() -> "Elige el tipo específico."
                general == null -> "Elige el tipo general primero."
                trimmedSpecific !in allowedSpecifics ->
                    "Ese tipo no pertenece a este grupo. Elige una opción del grupo."
                else -> null
            },
            color = when {
                trimmedColor.isEmpty() -> "El color es obligatorio."
                trimmedColor !in colorOptions ->
                    "Elige un color de la paleta."
                else -> null
            },
            fit = when {
                trimmedFit.isEmpty() -> null
                general == null -> "Elige el tipo general primero."
                allowedFits.isNotEmpty() && trimmedFit !in allowedFits ->
                    "Ese fit no es de este grupo. Elige una sugerencia del grupo o déjalo vacío."
                else -> null
            },
            notes = if (trimmedNotes.length > 500) {
                "Las observaciones admiten hasta 500 caracteres."
            } else {
                null
            },
            photo = photoError,
        )
        if (errors.hasErrors) return SaveResult.Rejected(errors)

        // Grupo sin opciones en catálogo: el selector muestra estado vacío
        // ("aún no hay opciones para este grupo") en vez de fallar; aquí el
        // guardado solo llega si el selector aportó un valor válido.
        checkNotNull(general)

        var copiedPath: String? = null
        try {
            if (photoUri != null && photoExt != null) {
                copiedPath = photos.copy(photoUri, photoExt)
            }
            val id = dao.insert(
                GarmentEntity(
                    name = trimmedName,
                    generalKey = general.key,
                    specific = trimmedSpecific,
                    color = trimmedColor,
                    fit = trimmedFit.ifEmpty { null },
                    notes = trimmedNotes,
                    photoPath = copiedPath,
                    ownerId = "local",
                    createdAt = System.currentTimeMillis(),
                ),
            )
            return SaveResult.Ok(id)
        } catch (e: Exception) {
            copiedPath?.let { photos.delete(it) }
            throw e
        }
    }
}
