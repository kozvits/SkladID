package com.kozvits.skladid.presentation

import androidx.compose.runtime.*
import com.kozvits.skladid.data.OpenRouterClient

@Composable
fun ModelSelector(client: OpenRouterClient) {
    val models = remember { client.fetchModels() }
    var selected by remember { mutableStateOf(models.firstOrNull() ?: "") }
    // dropdown placeholder
}
