package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.RecipientGroup
import java.util.UUID

fun interface FindGroupsByTenantPort {
    fun findByTenantId(tenantId: UUID): List<RecipientGroup>
}