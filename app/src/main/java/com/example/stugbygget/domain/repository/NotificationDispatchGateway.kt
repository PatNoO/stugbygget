package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.NotificationEvent

interface NotificationDispatchGateway {
    fun dispatch(events: List<NotificationEvent>)
}
