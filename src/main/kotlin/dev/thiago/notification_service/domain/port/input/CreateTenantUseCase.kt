package dev.thiago.notification_service.domain.port.input

import dev.thiago.notification_service.domain.model.Tenant

interface CreateTenantUseCase {
    fun create(name: String): Tenant
}
