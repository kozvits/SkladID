package com.kozvits.skladid.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kozvits.skladid.data.local.db.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY createdAtEpochMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProductEntity): Long

    @Delete
    suspend fun delete(entity: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT warehouse, rack, shelf, cell FROM products")
    suspend fun getAllOccupiedCells(): List<OccupiedCell>
}

/** Lightweight projection used only to compute free/occupied storage cells. */
data class OccupiedCell(
    val warehouse: String,
    val rack: String,
    val shelf: String,
    val cell: String
)
