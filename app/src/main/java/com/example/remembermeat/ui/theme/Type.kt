package com.example.remembermeat.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

private val base = Typography()

val AppTypography = Typography(
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
)