package dev.thiago.notification_service.infrastructure.persistence.template

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TemplateJpaRepository : JpaRepository<TemplateJpaEntity, UUID> {
    fun findByTenantId(tenantId: UUID): List<TemplateJpaEntity>
}