package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.NotificationEvent
import com.example.stugbygget.domain.repository.NotificationDispatchGateway

class DispatchNotificationEventsUseCase(
    private val gateway: NotificationDispatchGateway
) {
    operator fun invoke(events: List<NotificationEvent>) {
        gateway.dispatch(events)
    }
}
