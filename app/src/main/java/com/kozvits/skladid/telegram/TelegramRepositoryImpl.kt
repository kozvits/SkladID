package com.kozvits.skladid.telegram

import android.content.Context
import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.repository.TelegramRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelegramRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiClient: TelegramApiClient,
    private val formatter: ProductExportFormatter
) : TelegramRepository {

    override suspend fun sendProductList(
        products: List<Product>,
        botToken: String,
        chatId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.cacheDir, "skladid_products_${System.currentTimeMillis()}.json")
            file.writeText(formatter.toJson(products))

            try {
                apiClient.sendDocument(botToken, chatId, file, file.name).getOrThrow()

                val summary = formatter.toTextSummary(products)
                formatter.splitForTelegram(summary).forEach { chunk ->
                    apiClient.sendMessage(botToken, chatId, chunk).getOrThrow()
                }
            } finally {
                file.delete()
            }
        }
    }
}
