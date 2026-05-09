package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Notification

fun interface SaveNotificationPort {
    fun save(notification: Notification): Notification
}