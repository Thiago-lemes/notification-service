package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Tenant

fun interface SaveTenantPort {
    fun save(tenant: Tenant): Tenant
}