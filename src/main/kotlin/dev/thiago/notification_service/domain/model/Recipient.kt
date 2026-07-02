package dev.thiago.notification_service.domain.model

import java.util.*

data class Recipient(
    val id: UUID,
    val tenantId: UUID,
    val name: String,
    val email: String?,
    val phone: String?,
    val webhookUrl: String?,
    val channelPreferences: List<String>
)