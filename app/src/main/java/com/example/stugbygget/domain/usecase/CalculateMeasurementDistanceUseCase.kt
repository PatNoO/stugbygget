package com.example.stugbygget.domain.usecase

import kotlin.math.pow
import kotlin.math.sqrt

class CalculateMeasurementDistanceUseCase {

    /**
     * Baseline approximation for two-point measurement on camera overlay.
     * `pixelsToMetersScale` should be calibrated per device and distance to plane.
     */
    operator fun invoke(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        pixelsToMetersScale: Float
    ): Float {
        val deltaX = endX - startX
        val deltaY = endY - startY
        val distancePixels = sqrt(deltaX.pow(2) + deltaY.pow(2))
        return distancePixels * pixelsToMetersScale
    }
}
