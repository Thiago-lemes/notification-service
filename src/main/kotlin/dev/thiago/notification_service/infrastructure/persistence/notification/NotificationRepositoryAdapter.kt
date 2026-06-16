package dev.thiago.notification_service.infrastructure.persistence.notification

import dev.thiago.notification_service.domain.model.Notification
import dev.thiago.notification_service.domain.port.output.FindNotificationPort
import dev.thiago.notification_service.domain.port.output.SaveNotificationPort
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.UUID

@Primary
@Component
class NotificationRepositoryAdapter(
    private val jpaRepository: NotificationJpaRepository
) : SaveNotificationPort, FindNotificationPort {

    override fun save(notification: Notification): Notification {
        jpaRepository.save(notification.toEntity())
        return notification
    }

    override fun findById(id: UUID): Notification? {
        return jpaRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findByTenantId(tenantId: UUID, pageable: Pageable): Page<Notification> {
        return jpaRepository.findByTenantId(tenantId, pageable).map { it.toDomain() }
    }

    private fun Notification.toEntity() = NotificationJpaEntity(
        id = id,
        tenantId = tenantId,
        templateId = templateId,
        groupId = groupId,
        payload = payload,
        status = status
    )

    private fun NotificationJpaEntity.toDomain() = Notification(
        id = id,
        tenantId = tenantId,
        templateId = templateId,
        groupId = groupId,
        payload = payload,
        status = status
    )
}