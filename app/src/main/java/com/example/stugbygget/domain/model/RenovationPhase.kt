package com.example.stugbygget.domain.model

import java.time.Instant

data class RenovationPhase(
    val id: String,
    val name: String,
    val room: String,
    val startDate: Instant,
    val endDate: Instant,
    val progress: Int,
    val color: String,
    val icon: String
)
