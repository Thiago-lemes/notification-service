package dev.thiago.notification_service.infrastructure.web.dto

data class NotificationRequest(
    val templateId: String?,
    val groupId: String?,
    val payload: Map<String, Any>
)