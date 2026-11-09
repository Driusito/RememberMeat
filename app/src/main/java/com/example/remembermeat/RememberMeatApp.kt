package com.example.remembermeat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class RememberMeatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createChannel()
        scheduleDailyCheck()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            ExpiryWorker.CHANNEL_ID,
            "Avisos de caducidad",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun scheduleDailyCheck() {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(9, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delay = Duration.between(now, next)

        val request = PeriodicWorkRequestBuilder<ExpiryWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expiry-check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}