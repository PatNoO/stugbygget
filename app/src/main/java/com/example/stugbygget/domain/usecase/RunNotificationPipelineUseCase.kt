package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.RenovationPhase
import java.time.Instant

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
