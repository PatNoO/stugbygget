package com.example.stugbygget.domain.repository

interface ProjectSessionRepository {
    fun getProjectId(): String
    fun getCurrentUserId(): String?
}
