package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Recipient
import java.util.UUID

fun interface FindRecipientsByTenantPort {
    fun findByTenantId(tenantId: UUID): List<Recipient>
}