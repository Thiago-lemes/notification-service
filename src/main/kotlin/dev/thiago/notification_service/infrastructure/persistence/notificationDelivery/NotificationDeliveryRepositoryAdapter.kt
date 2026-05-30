package dev.thiago.notification_service.infrastructure.persistence.notificationDelivery

import dev.thiago.notification_service.domain.model.Notification
import dev.thiago.notification_service.domain.port.output.FindNotificationPort
import dev.thiago.notification_service.domain.port.output.SaveNotificationPort
import dev.thiago.notification_service.infrastructure.persistence.notification.NotificationJpaEntity
import dev.thiago.notification_service.infrastructure.persistence.notification.NotificationJpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class NotificationDeliveryRepositoryAdapter(
    private val jpaRepository: NotificationJpaRepository
) : SaveNotificationPort, FindNotificationPort {

    override fun save(notification: Notification): Notification {
        jpaRepository.save(notification.toEntity())
        return notification
    }

    override fun findById(id: UUID): Notification? {
        return jpaRepository.findById(id).orElse(null)?.toDomain()
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