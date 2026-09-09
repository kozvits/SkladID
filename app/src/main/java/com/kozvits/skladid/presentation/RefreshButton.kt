package com.kozvits.skladid.presentation

import androidx.compose.runtime.*
import com.kozvits.skladid.data.OpenRouterClient

@Composable
fun RefreshButton(client: OpenRouterClient) {
    Button(onClick = { client.fetchModels() }) {
        Text("Обновить модели")
    }
}
