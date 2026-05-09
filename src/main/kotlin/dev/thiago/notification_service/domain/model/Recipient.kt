package dev.thiago.notification_service.domain.model

import java.util.UUID

data class Recipient(
    val id: UUID,
    val tenantId: UUID,
    val name: String,
    val email: String?,
    val phone: String?,
    val channelPreferences: List<String>
)