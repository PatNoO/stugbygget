package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.RenovationPhase
import java.time.Instant

/**
 * Orchestrates the full notification pipeline in a single call.
 *
 * Execution order:
 * 1. Load user [NotificationSettings] via [GetNotificationSettingsUseCase].
 * 2. Evaluate current state (price changes, phase deadlines) via [BuildNotificationEventsUseCase].
 * 3. Dispatch any resulting events via [DispatchNotificationEventsUseCase] — only called
 *    when at least one event is produced, avoiding unnecessary FCM traffic.
 *
 * Designed to be called from a background worker or a scheduled Cloud Function trigger
 * after a price refresh or daily cron tick.
 *
 * @param watchedMaterials Set of material names the user is tracking for price drops.
 * @param previousPrices Last-known prices keyed by material name.
 * @param latestPrices Freshly fetched prices keyed by material name.
 * @param phases Current list of renovation phases for deadline checks.
 * @param now Reference instant for deadline calculations (defaults to [Instant.now]).
 */
class RunNotificationPipelineUseCase(
    private val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    private val buildNotificationEventsUseCase: BuildNotificationEventsUseCase,
    private val dispatchNotificationEventsUseCase: DispatchNotificationEventsUseCase
) {
    operator fun invoke(
        watchedMaterials: Set<String>,
        previousPrices: Map<String, Double>,
        latestPrices: Map<String, Double>,
        phases: List<RenovationPhase>,
        now: Instant = Instant.now()
    ) {
        val settings = getNotificationSettingsUseCase()
        val events = buildNotificationEventsUseCase.invoke(
            settings = settings,
            watchedMaterials = watchedMaterials,
            previousPrices = previousPrices,
            latestPrices = latestPrices,
            phases = phases,
            now = now
        )
        if (events.isNotEmpty()) {
            dispatchNotificationEventsUseCase(events)
        }
    }
}
