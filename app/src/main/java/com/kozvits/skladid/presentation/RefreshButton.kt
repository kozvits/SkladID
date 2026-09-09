package com.kozvits.skladid.presentation

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.kozvits.skladid.data.OpenRouterClient

@Composable
fun RefreshButton(client: OpenRouterClient) {
    Button(onClick = { client.refreshModels() }) {
        Text("Обновить модели")
    }
}