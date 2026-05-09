package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Notification

fun interface NotificationPublisherPort {
    fun publish(notification: Notification)
}