package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.RuntimeConfig

interface RuntimeConfigRepository {
    suspend fun fetchAndActivate(): RuntimeConfig
    fun getCached(): RuntimeConfig
}
