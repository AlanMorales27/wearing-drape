package com.wearingdrape.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Prenda del closet (tabla `garments`, spec 001 data-model.md).
 *
 * Cada fila es independiente; el nombre NO es único (duplicados permitidos).
 * `specific` y `fit` son snapshots del catálogo vigente al guardar (FR-013).
 * `photoPath` es ruta relativa en `filesDir/closet/`; null = sin foto.
 * `ownerId` es constante `"local"` en 001 (reserva para `user_id` + RLS futuros).
 */
@Entity(tableName = "garments")
data class GarmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Obligatorio, 1–60 caracteres. */
    val name: String,
    /** Enum como String: SUPERIOR | INFERIOR | CALZADO | ACCESORIO. Obligatorio. */
    val generalKey: String,
    /** Snapshot del catálogo; MUST ∈ specificsByGeneral[generalKey]. Obligatorio. */
    val specific: String,
    /** Texto libre descriptivo. Obligatorio, 1–40 caracteres. */
    val color: String,
    /** Snapshot opcional; si se informa y el grupo tiene sugerencias, MUST ∈ fitsByGeneral. */
    val fit: String? = null,
    /** Observaciones opcionales, 0–500 caracteres. */
    val notes: String = "",
    /** Ruta relativa en filesDir/closet/; 1 foto por prenda en v1. */
    val photoPath: String? = null,
    val ownerId: String = "local",
    val createdAt: Long = System.currentTimeMillis(),
)
