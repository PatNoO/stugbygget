package com.example.stugbygget.feature.logistics.ui

import com.example.stugbygget.domain.model.LogisticsCalculation
import com.example.stugbygget.domain.model.TransportType

data class LogisticsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val calculation: LogisticsCalculation? = null,
) {
    val recommendedType: TransportType?
        get() = calculation?.recommendedType
}
