package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.NotificationSettings

interface NotificationSettingsRepository {
    fun get(): NotificationSettings
    fun update(settings: NotificationSettings)
}
