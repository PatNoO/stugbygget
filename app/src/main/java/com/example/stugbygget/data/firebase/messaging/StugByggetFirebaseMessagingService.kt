package com.example.stugbygget.data.firebase.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.stugbygget.MainActivity
import com.example.stugbygget.R
import com.example.stugbygget.StugByggetApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID

/**
 * Handles FCM token refresh and incoming push messages.
 *
 * Token is stored at users/{userId}/fcmToken so that server-side Cloud Functions
 * can target specific users for phase-deadline and todo-update notifications.
 *
 * Expected FCM data payload keys:
 *   - title  : notification title
 *   - body   : notification body
 *   - screen : "planning" | "todos" — drives deep-link on tap
 */
class StugByggetFirebaseMessagingService : FirebaseMessagingService() {

    /** Persist the new token whenever Firebase rotates it. */
    override fun onNewToken(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
    }

    /** Called when a data/notification message arrives while the app is in the foreground,
     *  or when a data-only message arrives regardless of app state. */
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val title = data["title"] ?: message.notification?.title ?: getString(R.string.app_name)
        val body = data["body"] ?: message.notification?.body ?: ""
        val screen = data["screen"]
        val eventId = data["id"] ?: UUID.randomUUID().toString()

        ensureChannel()

        val pendingIntent = buildNavigationIntent(screen)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply { pendingIntent?.let { setContentIntent(it) } }
            .build()

        NotificationManagerCompat.from(this).notify(eventId.hashCode(), notification)
    }

    private fun buildNavigationIntent(screen: String?): PendingIntent? {
        screen ?: return null
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_NAVIGATE_TO, screen)
        }
        return PendingIntent.getActivity(
            this, screen.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Project Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Phase deadlines and todo updates."
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "stugbygget_alerts"
    }
}
