package dev.thiago.notification_service.infrastructure.web.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateTemplateRequest(
    @field:NotBlank val tenantId: String,
    @field:NotBlank val name: String,
    @field:NotBlank val channel: String,
    val subject: String?,
    @field:NotBlank val body: String
)