package com.wearingdrape.app

import android.app.Application
import androidx.room.Room
import com.wearingdrape.app.data.AppDatabase
import com.wearingdrape.app.data.FilePhotoDataSource
import com.wearingdrape.app.data.GarmentRepository
import com.wearingdrape.app.data.StaticCatalogDataSource

/**
 * Holder manual del grafo Data (sin Hilt/Koin por YAGNI, spec 001).
 * Expone el singleton de [AppDatabase] y [GarmentRepository] para la
 * factory manual de ViewModels.
 */
class WearingDrapeApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "wearing-drape.db").build()
    }

    val garmentRepository: GarmentRepository by lazy {
        GarmentRepository(
            dao = database.garmentDao(),
            photos = FilePhotoDataSource(this),
            catalog = StaticCatalogDataSource(),
        )
    }
}
