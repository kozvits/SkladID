package com.kozvits.skladid.di

import android.content.Context
import androidx.room.Room
import com.kozvits.skladid.data.local.db.SkladDatabase
import com.kozvits.skladid.data.local.db.dao.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSkladDatabase(@ApplicationContext context: Context): SkladDatabase =
        Room.databaseBuilder(context, SkladDatabase::class.java, SkladDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideProductDao(database: SkladDatabase): ProductDao = database.productDao()
}
