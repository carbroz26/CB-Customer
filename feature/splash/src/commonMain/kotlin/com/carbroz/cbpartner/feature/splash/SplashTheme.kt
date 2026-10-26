package com.carbroz.cbpartner.feature.splash

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SplashBlue = Color(0xFF176AD6)
private val SplashDeepBlue = Color(0xFF0A337F)
private val SplashSecondaryText = Color(0xFF6F8EC0)
private val SplashBackground = Color(0xFFF1F9FF)
private val SplashLightBlue = Color(0xFFE2F1FF)

@Composable
internal fun CarBrozSplashTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = SplashBlue,
            onPrimary = Color.White,
            primaryContainer = SplashLightBlue,
            onPrimaryContainer = SplashDeepBlue,
            surface = SplashBackground,
            onSurface = SplashDeepBlue,
            onSurfaceVariant = SplashSecondaryText,
            background = SplashBackground,
            onBackground = SplashDeepBlue,
        ),
        content = content,
    )
}
