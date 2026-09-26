package com.food.freshkeeper.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.food.freshkeeper.FoodApp
import com.food.freshkeeper.MainActivity
import com.food.freshkeeper.R

object NotificationHelper {

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FoodApp.CHANNEL_ID,
                "食品到期与临期预警",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "在食品临近保质期或已过期时发送提醒通知"
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * 发送一条真实的食品过期/临期预警通知
     */
    fun sendExpiryNotification(
        context: Context,
        foodName: String = "鲜牛奶 🥛",
        daysMessage: String = "已过期 1 天",
        location: String = "冷藏室 🧊",
        notificationId: Int = 1001
    ): Boolean {
        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚠️ 食物过期预警提醒"
        val content = "「$foodName」位于 $location，$daysMessage，请尽快饮用或妥善处理，避免浪费！"

        val builder = NotificationCompat.Builder(context, FoodApp.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        return try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(notificationId, builder.build())
                true
            } else {
                false
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
