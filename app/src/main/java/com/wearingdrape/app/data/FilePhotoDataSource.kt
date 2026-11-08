package com.wearingdrape.app.data

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Fotos en almacenamiento interno (spec 001 research Decision 2).
 *
 * Copia a `filesDir/closet/` como `garment_<timestamp>_<random>.<ext>`.
 * Valida JPEG/PNG/WebP y ≤ 5 MB (FR-007/FR-008).
 * Si el insert posterior falla, el repositorio borra el fichero copiado
 * (guardado atómico, FR-012).
 */
class FilePhotoDataSource(private val context: Context) {

    companion object {
        const val DIR = "closet"
        const val MAX_BYTES = 5L * 1024 * 1024
        private val EXT_BY_MIME = mapOf(
            "image/jpeg" to "jpg",
            "image/png" to "png",
            "image/webp" to "webp",
        )
    }

    /** Resultado de validación: extensión válida o error descriptivo. */
    sealed interface Validation {
        data class Ok(val ext: String) : Validation
        data class Rejected(val message: String) : Validation
    }

    fun validate(uri: Uri): Validation {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri)
        val ext = EXT_BY_MIME[mime]
            ?: return Validation.Rejected("Solo se aceptan imágenes JPEG, PNG o WebP.")
        val size = runCatching {
            resolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
        }.getOrNull() ?: -1
        if (size in 0..MAX_BYTES) return Validation.Ok(ext)
        if (size > MAX_BYTES) {
            return Validation.Rejected("La foto supera el máximo de 5 MB.")
        }
        // Tamaño desconocido (-1): se valida durante la copia por bytes escritos.
        return Validation.Ok(ext)
    }

    /** Copia el contenido de [uri] al directorio interno. Devuelve ruta relativa. */
    fun copy(uri: Uri, ext: String): String {
        val dir = File(context.filesDir, DIR).apply { mkdirs() }
        val name = "garment_${System.currentTimeMillis()}_${(0..9999).random()}.$ext"
        val dest = File(dir, name)
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output ->
                val copied = input.copyTo(output)
                if (copied > MAX_BYTES) {
                    dest.delete()
                    throw IllegalArgumentException("La foto supera el máximo de 5 MB.")
                }
            }
        } ?: throw IllegalArgumentException("No se pudo leer la imagen seleccionada.")
        return "$DIR/$name"
    }

    /** Borra un fichero copiado previamente (ruta relativa). */
    fun delete(relativePath: String) {
        runCatching { File(context.filesDir, relativePath).delete() }
    }

    /** Resuelve una ruta relativa a [File] absoluto (null si no existe). */
    fun resolve(relativePath: String?): File? {
        if (relativePath.isNullOrBlank()) return null
        return File(context.filesDir, relativePath).takeIf { it.exists() }
    }
}
