package com.kozvits.skladid.domain.usecase.settings

import com.kozvits.skladid.domain.repository.LabelSettings
import com.kozvits.skladid.domain.repository.PrinterSettings
import com.kozvits.skladid.domain.repository.SettingsRepository
import com.kozvits.skladid.domain.repository.TelegramSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetApiKeyUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(): String? = repo.getApiKey()
}

class SetApiKeyUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(apiKey: String) = repo.setApiKey(apiKey)
}

class ObserveSelectedModelIdUseCase @Inject constructor(private val repo: SettingsRepository) {
    operator fun invoke(): Flow<String?> = repo.observeSelectedModelId()
}

class SetSelectedModelIdUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(modelId: String) = repo.setSelectedModelId(modelId)
}

class ObservePrinterSettingsUseCase @Inject constructor(private val repo: SettingsRepository) {
    operator fun invoke(): Flow<PrinterSettings> = repo.observePrinterSettings()
}

class SetPrinterSettingsUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(settings: PrinterSettings) = repo.setPrinterSettings(settings)
}

class ObserveLabelSettingsUseCase @Inject constructor(private val repo: SettingsRepository) {
    operator fun invoke(): Flow<LabelSettings> = repo.observeLabelSettings()
}

class SetLabelSettingsUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(settings: LabelSettings) = repo.setLabelSettings(settings)
}

class GetTelegramBotTokenUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(): String? = repo.getTelegramBotToken()
}

class SetTelegramBotTokenUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(token: String) = repo.setTelegramBotToken(token)
}

class ObserveTelegramSettingsUseCase @Inject constructor(private val repo: SettingsRepository) {
    operator fun invoke(): Flow<TelegramSettings> = repo.observeTelegramSettings()
}

class SetTelegramChatIdUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(chatId: String) = repo.setTelegramChatId(chatId)
}
