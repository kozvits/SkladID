package com.kozvits.skladid.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kozvits.skladid.domain.repository.LabelSettings
import com.kozvits.skladid.domain.repository.PrinterConnectionType
import com.kozvits.skladid.domain.repository.PrinterSettings
import com.kozvits.skladid.domain.repository.SettingsRepository
import com.kozvits.skladid.domain.repository.TelegramSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "skladid_settings")

private const val ENCRYPTED_PREFS_NAME = "secure_settings"
private const val KEY_API_KEY = "openrouter_api_key"
private const val KEY_TELEGRAM_BOT_TOKEN = "telegram_bot_token"

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun getApiKey(): String? = withContext(Dispatchers.IO) {
        encryptedPrefs.getString(KEY_API_KEY, null)
    }

    override suspend fun setApiKey(apiKey: String) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    override fun observeSelectedModelId(): Flow<String?> =
        context.dataStore.data.map { it[Keys.SELECTED_MODEL_ID] }

    override suspend fun setSelectedModelId(modelId: String) {
        context.dataStore.edit { it[Keys.SELECTED_MODEL_ID] = modelId }
    }

    override fun observePrinterSettings(): Flow<PrinterSettings> =
        context.dataStore.data.map { prefs ->
            PrinterSettings(
                connectionType = prefs[Keys.PRINTER_CONNECTION_TYPE]
                    ?.let { runCatching { PrinterConnectionType.valueOf(it) }.getOrNull() }
                    ?: PrinterConnectionType.BLUETOOTH,
                bluetoothDeviceAddress = prefs[Keys.PRINTER_BT_ADDRESS],
                bluetoothDeviceName = prefs[Keys.PRINTER_BT_NAME],
                networkIp = prefs[Keys.PRINTER_IP],
                networkPort = prefs[Keys.PRINTER_PORT] ?: 9100
            )
        }

    override suspend fun setPrinterSettings(settings: PrinterSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PRINTER_CONNECTION_TYPE] = settings.connectionType.name
            settings.bluetoothDeviceAddress?.let { prefs[Keys.PRINTER_BT_ADDRESS] = it }
            settings.bluetoothDeviceName?.let { prefs[Keys.PRINTER_BT_NAME] = it }
            settings.networkIp?.let { prefs[Keys.PRINTER_IP] = it }
            prefs[Keys.PRINTER_PORT] = settings.networkPort
        }
    }

    override fun observeLabelSettings(): Flow<LabelSettings> =
        context.dataStore.data.map { prefs ->
            LabelSettings(
                widthMm = prefs[Keys.LABEL_WIDTH_MM]?.toFloatOrNull() ?: 40f,
                heightMm = prefs[Keys.LABEL_HEIGHT_MM]?.toFloatOrNull() ?: 30f,
                dpi = prefs[Keys.LABEL_DPI] ?: 203
            )
        }

    override suspend fun setLabelSettings(settings: LabelSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LABEL_WIDTH_MM] = settings.widthMm.toString()
            prefs[Keys.LABEL_HEIGHT_MM] = settings.heightMm.toString()
            prefs[Keys.LABEL_DPI] = settings.dpi
        }
    }

    override suspend fun getTelegramBotToken(): String? = withContext(Dispatchers.IO) {
        encryptedPrefs.getString(KEY_TELEGRAM_BOT_TOKEN, null)
    }

    override suspend fun setTelegramBotToken(token: String) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putString(KEY_TELEGRAM_BOT_TOKEN, token).apply()
    }

    override fun observeTelegramSettings(): Flow<TelegramSettings> =
        context.dataStore.data.map { prefs ->
            TelegramSettings(chatId = prefs[Keys.TELEGRAM_CHAT_ID])
        }

    override suspend fun setTelegramChatId(chatId: String) {
        context.dataStore.edit { prefs -> prefs[Keys.TELEGRAM_CHAT_ID] = chatId }
    }

    private object Keys {
        val SELECTED_MODEL_ID = stringPreferencesKey("selected_model_id")
        val PRINTER_CONNECTION_TYPE = stringPreferencesKey("printer_connection_type")
        val PRINTER_BT_ADDRESS = stringPreferencesKey("printer_bt_address")
        val PRINTER_BT_NAME = stringPreferencesKey("printer_bt_name")
        val PRINTER_IP = stringPreferencesKey("printer_ip")
        val PRINTER_PORT = intPreferencesKey("printer_port")
        val LABEL_WIDTH_MM = stringPreferencesKey("label_width_mm")
        val LABEL_HEIGHT_MM = stringPreferencesKey("label_height_mm")
        val LABEL_DPI = intPreferencesKey("label_dpi")
        val TELEGRAM_CHAT_ID = stringPreferencesKey("telegram_chat_id")
    }
}
