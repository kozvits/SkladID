package com.kozvits.skladid.domain.repository

import kotlinx.coroutines.flow.Flow

enum class PrinterConnectionType { BLUETOOTH, NETWORK }

data class PrinterSettings(
    val connectionType: PrinterConnectionType = PrinterConnectionType.BLUETOOTH,
    val bluetoothDeviceAddress: String? = null,
    val bluetoothDeviceName: String? = null,
    val networkIp: String? = null,
    val networkPort: Int = 9100
)

data class LabelSettings(
    val widthMm: Float = 40f,
    val heightMm: Float = 30f,
    val dpi: Int = 203
)

data class TelegramSettings(
    val chatId: String? = null
)

interface SettingsRepository {
    // API key is stored via EncryptedSharedPreferences — never exposed as plain Flow to avoid
    // accidental logging; read is a one-shot suspend call.
    suspend fun getApiKey(): String?
    suspend fun setApiKey(apiKey: String)

    fun observeSelectedModelId(): Flow<String?>
    suspend fun setSelectedModelId(modelId: String)

    fun observePrinterSettings(): Flow<PrinterSettings>
    suspend fun setPrinterSettings(settings: PrinterSettings)

    fun observeLabelSettings(): Flow<LabelSettings>
    suspend fun setLabelSettings(settings: LabelSettings)

    // Telegram bot token is a credential like the API key — encrypted, one-shot read only.
    suspend fun getTelegramBotToken(): String?
    suspend fun setTelegramBotToken(token: String)

    fun observeTelegramSettings(): Flow<TelegramSettings>
    suspend fun setTelegramChatId(chatId: String)
}
