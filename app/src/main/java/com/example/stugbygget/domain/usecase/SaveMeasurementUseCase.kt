package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.MeasurementRecord
import com.example.stugbygget.domain.model.MeasurementType
import com.example.stugbygget.domain.repository.MeasurementRepository

class SaveMeasurementUseCase(
    private val repository: MeasurementRepository,
    private val converter: MeasurementUnitConverter
) {
    suspend operator fun invoke(
        projectId: String,
        label: String,
        valueMeters: Float,
        type: MeasurementType
    ) {
        val valueCm = converter.metersToRoundedCentimeters(valueMeters)
        repository.saveMeasurement(
            projectId = projectId,
            measurement = MeasurementRecord(
                label = label,
                valueCm = valueCm,
                type = type
            )
        )
    }
}
