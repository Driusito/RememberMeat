package com.example.remembermeat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    onSurfaceVariant = Color(0xFF55645C),
    outlineVariant = Color(0xFFDCE3DD),
    error = Urgent,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FCF9F),
    onPrimary = Color(0xFF07281B),
    background = Color(0xFF101915),
    onBackground = Color(0xFFE6EEE9),
    surface = Color(0xFF18231D),
    onSurface = Color(0xFFE6EEE9),
    onSurfaceVariant = Color(0xFFA5B5AC),
    outlineVariant = Color(0xFF2A3830),
    error = Color(0xFFE0645A),
    onError = Color.White
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun RememberMeatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        content = content
    )
}