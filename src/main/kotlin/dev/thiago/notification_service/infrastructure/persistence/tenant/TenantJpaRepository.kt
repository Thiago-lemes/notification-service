package dev.thiago.notification_service.infrastructure.persistence.tenant

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TenantJpaRepository : JpaRepository<TenantJpaEntity, UUID> {
    fun findByApiKey(apiKey: String): TenantJpaEntity?
}