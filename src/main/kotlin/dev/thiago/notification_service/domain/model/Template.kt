package dev.thiago.notification_service.domain.model

import java.util.*

data class Template(
    val id: UUID = UUID.randomUUID(),
    val tenantId: UUID,
    val name: String,
    val channel: String,
    val subject: String?,
    val body: String
)