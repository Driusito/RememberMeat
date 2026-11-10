package com.example.remembermeat

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.remembermeat.database.AppDatabase
import java.time.LocalDate

class ExpiryWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val products = AppDatabase.get(context).productDao()
            .expiringBetween(today.toEpochDay(), today.plusDays(1).toEpochDay())
        if (products.isEmpty()) return Result.success()

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return Result.success()

        val todayCount = products.count { it.expiryDate == today }
        val total = products.size
        val title = when {
            todayCount == total ->
                if (total == 1) "1 producto caduca hoy" else "$total productos caducan hoy"
            todayCount == 0 ->
                if (total == 1) "1 producto caduca mañana" else "$total productos caducan mañana"
            else -> "$total productos caducan hoy o mañana"
        }
        val body = products.joinToString("\n") {
            val whenText = if (it.expiryDate == today) "Hoy" else "Mañana"
            "$whenText: ${it.name} en ${it.store}, ${formatPrice(it.priceCents)}"
        }

        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // temporal
            .setContentTitle(title)
            .setContentText(body.lineSequence().first())
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "expiry"
        const val NOTIFICATION_ID = 1
    }
}