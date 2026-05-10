package dev.thiago.notification_service.infrastructure.web.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateTenantRequest(
    @field:NotBlank(message = "Name is required")
    val name: String
)