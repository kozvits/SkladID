package com.kozvits.skladid.di

import com.kozvits.skladid.data.local.MlKitRecognitionRepositoryImpl
import com.kozvits.skladid.data.remote.OpenRouterApiClient
import com.kozvits.skladid.data.repository.ProductRepositoryImpl
import com.kozvits.skladid.data.repository.SettingsRepositoryImpl
import com.kozvits.skladid.data.repository.WarehouseRepositoryImpl
import com.kozvits.skladid.domain.repository.LocalRecognitionRepository
import com.kozvits.skladid.domain.repository.OpenRouterRepository
import com.kozvits.skladid.domain.repository.LabelRepository
import com.kozvits.skladid.domain.repository.PrinterRepository
import com.kozvits.skladid.label.LabelRepositoryImpl
import com.kozvits.skladid.printer.PrinterRepositoryImpl
import com.kozvits.skladid.domain.repository.ProductRepository
import com.kozvits.skladid.domain.repository.SettingsRepository
import com.kozvits.skladid.domain.repository.WarehouseRepository
import com.kozvits.skladid.domain.repository.TelegramRepository
import com.kozvits.skladid.telegram.TelegramRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindLocalRecognitionRepository(impl: MlKitRecognitionRepositoryImpl): LocalRecognitionRepository

    @Binds
    @Singleton
    abstract fun bindLabelRepository(impl: LabelRepositoryImpl): LabelRepository

    @Binds
    @Singleton
    abstract fun bindPrinterRepository(impl: PrinterRepositoryImpl): PrinterRepository

    @Binds
    @Singleton
    abstract fun bindTelegramRepository(impl: TelegramRepositoryImpl): TelegramRepository
}
