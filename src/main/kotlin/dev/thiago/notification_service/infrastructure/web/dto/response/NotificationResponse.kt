package dev.thiago.notification_service.infrastructure.web.dto.response

import dev.thiago.notification_service.domain.model.Notification
import java.util.UUID

data class NotificationResponse(
    val id: UUID,
    val status: String,
    val payload: Map<String, Any>,
    val tenantId: UUID
) {
    companion object {
        fun from(notification: Notification) = NotificationResponse(
            id = notification.id,
            status = notification.status,
            payload = notification.payload,
            tenantId = notification.tenantId
        )
    }
}