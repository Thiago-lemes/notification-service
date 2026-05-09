package dev.thiago.notification_service.domain.model

import java.util.UUID

data class NotificationEvent(
    val notificationId: UUID,
    val tenantId: UUID,
    val payload: Map<String, Any>
)