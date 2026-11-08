package com.wearingdrape.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Base Room local (spec 001). version 1.
 *
 * Política `exportSchema`: `false` en 001 — sin migraciones versionadas en
 * el MVP local de un solo usuario; si 001+ requiere migración, se activa
 * con `exportSchema = true` + directorio de esquemas.
 */
@Database(entities = [GarmentEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun garmentDao(): GarmentDao
}
