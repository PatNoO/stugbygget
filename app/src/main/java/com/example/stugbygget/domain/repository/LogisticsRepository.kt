package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.LogisticsCalculation

interface LogisticsRepository {
    suspend fun saveCalculation(projectId: String, calculation: LogisticsCalculation)
}
