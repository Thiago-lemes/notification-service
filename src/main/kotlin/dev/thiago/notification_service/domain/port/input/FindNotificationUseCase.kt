package dev.thiago.notification_service.domain.port.input

import dev.thiago.notification_service.domain.model.Notification
import java.util.UUID

fun interface FindNotificationUseCase {
    fun find(apiKey: String, notificationId: UUID): Notification
}