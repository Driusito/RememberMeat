package com.example.remembermeat

import android.Manifest
import android.R
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val tomorrow = LocalDate.now().plusDays(1).toEpochDay()
        val products = AppDatabase.get(context).productDao().expiringOn(tomorrow)
        if (products.isEmpty()) return Result.success()

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return Result.success()

        val title = if (products.size == 1) "1 producto caduca mañana"
        else "${products.size} productos caducan mañana"
        val body = products.joinToString("\n") { "${it.name} (${it.store}) · ${formatPrice(it.priceCents)}" }

        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info) // temporal
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