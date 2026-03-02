package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.NotificationEvent
import com.example.stugbygget.domain.model.NotificationSettings
import com.example.stugbygget.domain.model.NotificationType
import com.example.stugbygget.domain.model.RenovationPhase
import java.time.Duration
import java.time.Instant
import java.util.UUID

class BuildNotificationEventsUseCase {
    fun invoke(
        settings: NotificationSettings,
        watchedMaterials: Set<String>,
        previousPrices: Map<String, Double>,
        latestPrices: Map<String, Double>,
        phases: List<RenovationPhase>,
        now: Instant
    ): List<NotificationEvent> {
        val events = mutableListOf<NotificationEvent>()

        if (settings.priceDropEnabled) {
            watchedMaterials.forEach { material ->
                val previous = previousPrices[material] ?: return@forEach
                val latest = latestPrices[material] ?: return@forEach
                if (latest < previous) {
                    events += NotificationEvent(
                        id = UUID.randomUUID().toString(),
                        type = NotificationType.PRICE_DROP,
                        title = "Price drop detected",
                        body = "$material dropped from ${previous.toInt()} SEK to ${latest.toInt()} SEK."
                    )
                }
            }
        }

        phases.forEach { phase ->
            val daysToStart = Duration.between(now, phase.startDate).toDays()
            val daysToEnd = Duration.between(now, phase.endDate).toDays()

            if (settings.deliveryReminderEnabled && daysToStart in 0..2) {
                events += NotificationEvent(
                    id = UUID.randomUUID().toString(),
                    type = NotificationType.DELIVERY_REMINDER,
                    title = "Delivery reminder",
                    body = "Order materials for ${phase.name} before ${phase.startDate}."
                )
            }

            if (settings.phaseDeadlineEnabled && daysToEnd in 0..1) {
                events += NotificationEvent(
                    id = UUID.randomUUID().toString(),
                    type = NotificationType.PHASE_DEADLINE,
                    title = "Phase deadline approaching",
                    body = "${phase.name} is close to deadline (${phase.endDate})."
                )
            }
        }

        return events
    }
}
