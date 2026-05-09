package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Recipient

interface NotificationChannelPort {
    fun supports(channel: String): Boolean
    fun deliver(recipient: Recipient, payload: Map<String, Any>)
}