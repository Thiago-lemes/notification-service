package dev.thiago.notification_service.domain.model

import java.util.*

data class Tenant(
    val id: UUID,
    val name: String,
    val apiKey: String,
    val status: String
)