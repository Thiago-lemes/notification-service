package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Tenant
import java.util.*

interface TenantRepository {
    fun findByApiKey(apiKey: String): Tenant?
    fun findById(id: UUID): Tenant?
}