package dev.thiago.notification_service.infrastructure.web.dto.response

import dev.thiago.notification_service.domain.model.RecipientGroup
import java.util.*

data class GroupResponse(
    val id: UUID,
    val tenantId: UUID,
    val name: String,
    val description: String?
) {
    companion object {
        fun from(group: RecipientGroup) = GroupResponse(
            id = group.id,
            tenantId = group.tenantId,
            name = group.name,
            description = group.description
        )
    }
}