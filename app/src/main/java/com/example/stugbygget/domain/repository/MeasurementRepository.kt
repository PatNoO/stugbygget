package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.MeasurementRecord

interface MeasurementRepository {
    suspend fun saveMeasurement(projectId: String, measurement: MeasurementRecord)
}
