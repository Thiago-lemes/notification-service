package dev.thiago.notification_service.infrastructure.persistence.notification

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface NotificationJpaRepository : JpaRepository<NotificationJpaEntity, UUID> {
    fun findByTenantId(tenantId: UUID, pageable: Pageable): Page<NotificationJpaEntity>
}