package dev.thiago.notification_service.domain.model

import java.util.*

data class NotificationDelivery(
    val id: UUID = UUID.randomUUID(),
    val notificationId: UUID,
    val recipientId: UUID,
    val channel: String,
    val status: String = "PENDING",
    val attemptCount: Int = 0,
    val errorMessage: String? = null
)