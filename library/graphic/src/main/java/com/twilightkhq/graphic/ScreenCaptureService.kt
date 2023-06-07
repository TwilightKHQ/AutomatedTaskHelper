package com.twilightkhq.graphic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

/**
 * TODO 简化调整
 */
class ScreenCaptureService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, ScreenCaptureRequestActivity::class.java), flags
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Recording")
            .setWhen(System.currentTimeMillis())
            .setContentIntent(contentIntent)
            .setChannelId(CHANNEL_ID)
            .setVibrate(LongArray(0))
            .build()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        val name: CharSequence = "Recoding"
        val description = "Recoding"
        val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = description
        channel.enableLights(false)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    companion object {
        private const val NOTIFICATION_ID = 2
        private val CHANNEL_ID = ScreenCaptureService::class.java.simpleName + ".foreground"
    }
}