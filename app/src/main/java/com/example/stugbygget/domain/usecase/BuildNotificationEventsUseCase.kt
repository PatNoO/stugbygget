package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.NotificationEvent
import com.example.stugbygget.domain.model.NotificationSettings
import com.example.stugbygget.domain.model.NotificationType
import com.example.stugbygget.domain.model.RenovationPhase
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Builds a list of push notification events by comparing current state against
 * user notification preferences.
 *
 * Three event types are produced:
 * - [NotificationType.PRICE_DROP] — emitted when a watched material's latest price
 *   is lower than the previously recorded price.
 * - [NotificationType.DELIVERY_REMINDER] — emitted when a phase starts within 0–2 days,
 *   prompting the user to ensure materials are ordered.
 * - [NotificationType.PHASE_DEADLINE] — emitted when a phase end date is within 0–1 days.
 *
 * Each condition is gated by the corresponding flag in [NotificationSettings], allowing
 * users to opt in/out of each notification type independently.
 *
 * This use case is pure (no side effects) — it only builds the event list.
 * Dispatching is handled separately by [DispatchNotificationEventsUseCase].
 */
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
