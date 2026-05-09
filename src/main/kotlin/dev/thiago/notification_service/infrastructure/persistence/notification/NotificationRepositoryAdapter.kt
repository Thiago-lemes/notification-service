package dev.thiago.notification_service.infrastructure.persistence.notification

import dev.thiago.notification_service.domain.model.Notification
import dev.thiago.notification_service.domain.port.output.SaveNotificationPort
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class NotificationRepositoryAdapter(
    private val jpaRepository: NotificationJpaRepository
) : SaveNotificationPort {

    override fun save(notification: Notification): Notification {
        val entity = notification.toEntity()
        jpaRepository.save(entity)
        return notification
    }

    private fun Notification.toEntity() = NotificationJpaEntity(
        id = id,
        tenantId = tenantId,
        templateId = templateId,
        groupId = groupId,
        payload = payload,
        status = status
    )
}