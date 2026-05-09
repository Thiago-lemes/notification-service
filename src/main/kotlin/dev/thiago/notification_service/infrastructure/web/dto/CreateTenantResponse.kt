package dev.thiago.notification_service.infrastructure.web.dto

import java.util.UUID

data class CreateTenantResponse(
    val id: UUID,
    val name: String,
    val apiKey: String,
    val status: String
)