package com.wearingdrape.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GarmentDao {
    @Insert
    suspend fun insert(entity: GarmentEntity): Long

    @Query("SELECT * FROM garments ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<GarmentEntity>>
}
