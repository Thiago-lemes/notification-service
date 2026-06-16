package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface FindNotificationPort {
    fun findById(id: UUID): Notification?
    fun findByTenantId(tenantId: UUID, pageable: Pageable): Page<Notification>
}