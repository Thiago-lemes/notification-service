package dev.thiago.notification_service.domain.model

import java.util.UUID

data class RecipientGroup(
    val id: UUID = UUID.randomUUID(),
    val tenantId: UUID,
    val name: String,
    val description: String?
)