package com.example.stugbygget.data.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.stugbygget.R
import com.example.stugbygget.domain.model.NotificationEvent
import com.example.stugbygget.domain.repository.NotificationDispatchGateway

class AndroidNotificationDispatcher(
    private val context: Context
) : NotificationDispatchGateway {

    init {
        ensureChannel()
    }

    override fun dispatch(events: List<NotificationEvent>) {
        val manager = NotificationManagerCompat.from(context)
        events.forEachIndexed { index, event ->
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(event.title)
                .setContentText(event.body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            manager.notify(event.id.hashCode() + index, notification)
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Project Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Price drops, delivery reminders, and phase deadlines."
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "stugbygget_alerts"
    }
}
