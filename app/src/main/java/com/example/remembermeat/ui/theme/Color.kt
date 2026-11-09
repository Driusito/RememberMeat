package com.example.remembermeat.ui.theme

import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF17231E)
val Paper = Color(0xFFF4F6F1)
val Green = Color(0xFF1F5C45)

val Sticker = Color(0xFFFFD21F)
val StickerInk = Color(0xFF1A1A1A)

val Urgent = Color(0xFFC8362B)
val Soon = Color(0xFFD9731A)
val Fine = Color(0xFF2F7D4F)

fun urgencyColor(days: Long): Color = when {
    days <= 1L -> Urgent
    days <= 3L -> Soon
    else -> Fine
}