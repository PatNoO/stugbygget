package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.NotificationSettings
import com.example.stugbygget.domain.repository.NotificationSettingsRepository

class GetNotificationSettingsUseCase(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(): NotificationSettings = repository.get()
}
