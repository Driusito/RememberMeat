package com.example.remembermeat

import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

fun daysLeft(expiry: LocalDate): Long =
    ChronoUnit.DAYS.between(LocalDate.now(), expiry)

fun expiryText(days: Long): String = when {
    days < 0L -> if (days == -1L) "Caducó ayer" else "Caducó hace ${-days} días"
    days == 0L -> "Caduca hoy"
    days == 1L -> "Caduca mañana"
    else -> "Quedan $days días"
}

fun formatPrice(cents: Int): String =
    String.format(Locale("es", "ES"), "%.2f €", cents / 100.0)

fun parseCents(text: String): Int? =
    text.trim().replace(',', '.').toBigDecimalOrNull()
        ?.takeIf { it.signum() >= 0 }
        ?.movePointRight(2)
        ?.setScale(0, RoundingMode.HALF_UP)
        ?.toInt()