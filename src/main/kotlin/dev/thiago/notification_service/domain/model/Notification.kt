package dev.thiago.notification_service.domain.model

import java.util.*

data class Notification(
    val id: UUID = UUID.randomUUID(),
    val tenantId: UUID,
    val templateId: UUID?,
    val groupId: UUID?,
    val payload: Map<String, Any>,
    val status: String = "PENDING"
)
