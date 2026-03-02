package com.example.stugbygget.data.local

import android.content.Context
import com.example.stugbygget.domain.model.NotificationSettings
import com.example.stugbygget.domain.repository.NotificationSettingsRepository

class LocalNotificationSettingsRepository(
    context: Context
) : NotificationSettingsRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun get(): NotificationSettings {
        return NotificationSettings(
            priceDropEnabled = prefs.getBoolean(KEY_PRICE_DROP, true),
            deliveryReminderEnabled = prefs.getBoolean(KEY_DELIVERY, true),
            phaseDeadlineEnabled = prefs.getBoolean(KEY_DEADLINE, true)
        )
    }

    override fun update(settings: NotificationSettings) {
        prefs.edit()
            .putBoolean(KEY_PRICE_DROP, settings.priceDropEnabled)
            .putBoolean(KEY_DELIVERY, settings.deliveryReminderEnabled)
            .putBoolean(KEY_DEADLINE, settings.phaseDeadlineEnabled)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "notification_settings"
        private const val KEY_PRICE_DROP = "price_drop_enabled"
        private const val KEY_DELIVERY = "delivery_reminder_enabled"
        private const val KEY_DEADLINE = "phase_deadline_enabled"
    }
}
