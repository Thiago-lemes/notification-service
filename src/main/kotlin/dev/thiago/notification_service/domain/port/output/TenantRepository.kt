package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Tenant

fun interface TenantRepository {
    fun findByApiKey(apiKey: String): Tenant?
}