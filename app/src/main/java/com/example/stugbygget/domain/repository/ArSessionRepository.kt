package com.example.stugbygget.domain.repository

interface ArSessionRepository {
    fun isSupported(): Boolean
    fun startSession(): Result<Unit>
    fun stopSession()
}
