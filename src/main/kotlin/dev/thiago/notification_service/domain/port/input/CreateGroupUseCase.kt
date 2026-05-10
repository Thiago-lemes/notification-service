package dev.thiago.notification_service.domain.port.input

import dev.thiago.notification_service.domain.model.RecipientGroup

fun interface CreateGroupUseCase {
    fun create(request: CreateGroupRequest): RecipientGroup
}

data class CreateGroupRequest(
    val tenantId: String,
    val name: String,
    val description: String?
)