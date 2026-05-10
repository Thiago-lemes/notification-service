package dev.thiago.notification_service.infrastructure.web.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateGroupRequest(
    @field:NotBlank(message = "tenantId is required")
    val tenantId: String,

    @field:NotBlank(message = "name is required")
    val name: String,

    val description: String?
)