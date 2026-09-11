package com.kozvits.skladid.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = SkladOrange,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = SkladNavy,
    background = SkladBackgroundLight,
    surface = androidx.compose.ui.graphics.Color.White,
    error = SkladError
)

private val DarkColors = darkColorScheme(
    primary = SkladOrange,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = SkladNavyLight,
    background = SkladBackgroundDark,
    surface = SkladNavy,
    error = SkladError
)

@Composable
fun SkladIDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SkladTypography,
        content = content
    )
}
