package com.retirada.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.retirada.app.domain.ThemeMode

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6C7BFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2B3680),
    onPrimaryContainer = Color(0xFFDDE1FF),
    secondary = Color(0xFFF5B83D),
    tertiary = Color(0xFF4ADE80),
    background = Color(0xFF0B1020),
    onBackground = Color(0xFFE8EAFB),
    surface = Color(0xFF131A33),
    onSurface = Color(0xFFE8EAFB),
    surfaceVariant = Color(0xFF1C2547),
    onSurfaceVariant = Color(0xFFAAB2D5),
    outlineVariant = Color(0xFF2A335C),
    error = Color(0xFFFF6B6B),
    errorContainer = Color(0xFF4A1F2A),
    onErrorContainer = Color(0xFFFFD9DC)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF4C5CE8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E3FF),
    onPrimaryContainer = Color(0xFF16205F),
    secondary = Color(0xFFB57A00),
    tertiary = Color(0xFF1E8E4E),
    background = Color(0xFFF5F6FC),
    onBackground = Color(0xFF151A33),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF151A33),
    surfaceVariant = Color(0xFFE9ECF9),
    onSurfaceVariant = Color(0xFF4A5170),
    outlineVariant = Color(0xFFCDD2EA),
    error = Color(0xFFC62828),
    errorContainer = Color(0xFFFFE1E3),
    onErrorContainer = Color(0xFF5A0E14)
)

@Composable
fun RetiradaTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
