package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Notification
import java.util.UUID

interface FindNotificationPort {
    fun findById(id: UUID): Notification?
}