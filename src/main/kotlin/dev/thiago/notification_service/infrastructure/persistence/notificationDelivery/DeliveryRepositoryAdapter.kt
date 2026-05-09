package dev.thiago.notification_service.infrastructure.persistence.notificationDelivery

import dev.thiago.notification_service.domain.model.NotificationDelivery
import dev.thiago.notification_service.domain.port.output.SaveDeliveryPort
import org.springframework.stereotype.Component

@Component
class DeliveryRepositoryAdapter(
    private val jpaRepository: NotificationDeliveryJpaRepository
) : SaveDeliveryPort {

    override fun save(delivery: NotificationDelivery): NotificationDelivery {
        jpaRepository.save(delivery.toEntity())
        return delivery
    }

    private fun NotificationDelivery.toEntity() = NotificationDeliveryJpaEntity(
        id = id,
        notificationId = notificationId,
        recipientId = recipientId,
        channel = channel,
        status = status,
        attemptCount = attemptCount,
        errorMessage = errorMessage
    )
}