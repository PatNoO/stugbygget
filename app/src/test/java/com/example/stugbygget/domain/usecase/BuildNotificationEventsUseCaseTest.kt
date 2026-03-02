package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.NotificationSettings
import com.example.stugbygget.domain.model.NotificationType
import com.example.stugbygget.domain.model.RenovationPhase
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildNotificationEventsUseCaseTest {

    private val useCase = BuildNotificationEventsUseCase()

    @Test
    fun `builds price drop and schedule events when enabled`() {
        val now = Instant.parse("2026-06-01T00:00:00Z")
        val phase = RenovationPhase(
            id = "phase-1",
            name = "Roof Work",
            room = "Roof",
            startDate = Instant.parse("2026-06-02T00:00:00Z"),
            endDate = Instant.parse("2026-06-02T23:59:59Z"),
            progress = 0,
            color = "#000000",
            icon = ""
        )

        val events = useCase.invoke(
            settings = NotificationSettings(
                priceDropEnabled = true,
                deliveryReminderEnabled = true,
                phaseDeadlineEnabled = true
            ),
            watchedMaterials = setOf("Insulation"),
            previousPrices = mapOf("Insulation" to 500.0),
            latestPrices = mapOf("Insulation" to 450.0),
            phases = listOf(phase),
            now = now
        )

        assertTrue(events.any { it.type == NotificationType.PRICE_DROP })
        assertTrue(events.any { it.type == NotificationType.DELIVERY_REMINDER })
        assertTrue(events.any { it.type == NotificationType.PHASE_DEADLINE })
    }

    @Test
    fun `respects opt in settings`() {
        val events = useCase.invoke(
            settings = NotificationSettings(
                priceDropEnabled = false,
                deliveryReminderEnabled = false,
                phaseDeadlineEnabled = false
            ),
            watchedMaterials = setOf("Paint"),
            previousPrices = mapOf("Paint" to 200.0),
            latestPrices = mapOf("Paint" to 150.0),
            phases = emptyList(),
            now = Instant.now()
        )

        assertEquals(0, events.size)
    }
}
