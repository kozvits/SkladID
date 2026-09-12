package com.kozvits.skladid.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kozvits.skladid.data.local.db.dao.ProductDao
import com.kozvits.skladid.data.local.db.entity.ProductEntity

@Database(
    entities = [ProductEntity::class],
    version = 1,
    exportSchema = true
)
abstract class SkladDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        const val DATABASE_NAME = "skladid.db"
    }
}
