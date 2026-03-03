package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.RenovationPhase
import java.time.Instant
import kotlin.math.roundToInt

data class PlanningOverview(
    val totalProgressPercent: Int,
    val daysLeft: Long,
    val isSchedulePassed: Boolean
)

class BuildPlanningOverviewUseCase {
    operator fun invoke(
        phases: List<RenovationPhase>,
        now: Instant = Instant.now()
    ): PlanningOverview {
        if (phases.isEmpty()) {
            return PlanningOverview(
                totalProgressPercent = 0,
                daysLeft = 0L,
                isSchedulePassed = false
            )
        }

        val totalProgress = phases.map { phase -> phase.progress }.average().roundToInt()
        val maxEndDate = phases.maxOf { phase -> phase.endDate }
        val daysLeft = (maxEndDate.epochSecond - now.epochSecond) / 86_400L

        return PlanningOverview(
            totalProgressPercent = totalProgress,
            daysLeft = daysLeft,
            isSchedulePassed = daysLeft <= 0L
        )
    }
}
