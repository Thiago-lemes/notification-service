package dev.thiago.notification_service.infrastructure.web.dto.response

import java.util.*

data class CreateTenantResponse(
    val id: UUID,
    val name: String,
    val apiKey: String,
    val status: String
)