package dev.thiago.notification_service.infrastructure.messaging

import dev.thiago.notification_service.domain.model.Notification
import dev.thiago.notification_service.domain.model.NotificationEvent
import dev.thiago.notification_service.domain.port.output.NotificationPublisherPort
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class RabbitMQNotificationPublisher(
    private val rabbitTemplate: RabbitTemplate
) : NotificationPublisherPort {

    override fun publish(notification: Notification) {
        val event = NotificationEvent(
            notificationId = notification.id,
            tenantId = notification.tenantId,
            payload = notification.payload
        )

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE,
            RabbitMQConfig.ROUTING_KEY,
            event
        )
    }
}