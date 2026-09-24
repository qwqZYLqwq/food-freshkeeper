package com.food.freshkeeper

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class FoodApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "食材到期提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "在食品临近保质期时发送贴心提醒"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "food_freshness_alerts"
    }
}
