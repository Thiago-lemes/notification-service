package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.NotificationDelivery

interface SaveDeliveryPort {
    fun save(delivery: NotificationDelivery): NotificationDelivery
}