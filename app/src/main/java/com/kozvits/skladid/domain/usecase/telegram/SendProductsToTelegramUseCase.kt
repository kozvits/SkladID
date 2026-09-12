package com.kozvits.skladid.domain.usecase.telegram

import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.repository.SettingsRepository
import com.kozvits.skladid.domain.repository.TelegramRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SendProductsToTelegramUseCase @Inject constructor(
    private val telegramRepository: TelegramRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(products: List<Product>): Result<Unit> {
        val botToken = settingsRepository.getTelegramBotToken()?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Не задан Bot Token в настройках"))
        val chatId = settingsRepository.observeTelegramSettings().first().chatId?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Не задан Chat ID в настройках"))

        return telegramRepository.sendProductList(products, botToken, chatId)
    }
}
