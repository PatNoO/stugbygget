package com.example.stugbygget.domain.model

enum class NotificationType {
    PRICE_DROP,
    DELIVERY_REMINDER,
    PHASE_DEADLINE
}

data class NotificationSettings(
    val priceDropEnabled: Boolean = true,
    val deliveryReminderEnabled: Boolean = true,
    val phaseDeadlineEnabled: Boolean = true
)

data class NotificationEvent(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String
)
