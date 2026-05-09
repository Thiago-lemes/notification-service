package dev.thiago.notification_service.infrastructure.persistence.recipient

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecipientJpaRepository : JpaRepository<RecipientJpaEntity, UUID> {
    fun findByTenantId(tenantId: UUID): List<RecipientJpaEntity>
}