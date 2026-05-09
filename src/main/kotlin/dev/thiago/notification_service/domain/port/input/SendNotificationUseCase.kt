package dev.thiago.notification_service.domain.port.input

import dev.thiago.notification_service.domain.model.Notification

fun interface SendNotificationUseCase {
    fun send(apiKey: String, request: SendNotificationRequest): Notification
}

data class SendNotificationRequest(
    val templateId: String?,
    val groupId: String?,
    val payload: Map<String, Any>
)