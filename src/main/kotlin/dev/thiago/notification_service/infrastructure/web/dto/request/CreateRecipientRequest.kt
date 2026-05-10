package dev.thiago.notification_service.infrastructure.web.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class CreateRecipientRequest(
    @field:NotBlank(message = "tenantId is required")
    val tenantId: String,

    @field:NotBlank(message = "name is required")
    val name: String,

    val email: String?,
    val phone: String?,

    @field:NotEmpty(message = "channelPreferences must have at least one value")
    val channelPreferences: List<String>
)