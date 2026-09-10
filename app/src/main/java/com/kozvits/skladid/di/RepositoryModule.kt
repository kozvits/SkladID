package com.kozvits.skladid.di

import com.kozvits.skladid.data.remote.OpenRouterApiClient
import com.kozvits.skladid.data.repository.ProductRepositoryImpl
import com.kozvits.skladid.data.repository.SettingsRepositoryImpl
import com.kozvits.skladid.data.repository.WarehouseRepositoryImpl
import com.kozvits.skladid.domain.repository.OpenRouterRepository
import com.kozvits.skladid.domain.repository.ProductRepository
import com.kozvits.skladid.domain.repository.SettingsRepository
import com.kozvits.skladid.domain.repository.WarehouseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindWarehouseRepository(impl: WarehouseRepositoryImpl): WarehouseRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindOpenRouterRepository(impl: OpenRouterApiClient): OpenRouterRepository
}
