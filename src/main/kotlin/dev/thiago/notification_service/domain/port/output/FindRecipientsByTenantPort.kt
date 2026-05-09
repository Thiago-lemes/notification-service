package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Recipient
import java.util.UUID

interface FindRecipientsByTenantPort {
    fun findByTenantId(tenantId: UUID): List<Recipient>
}