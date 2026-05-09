package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.NotificationDelivery

fun interface SaveDeliveryPort {
    fun save(delivery: NotificationDelivery): NotificationDelivery
}