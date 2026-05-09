package dev.thiago.notification_service.infrastructure.messaging

import dev.thiago.notification_service.application.usecase.DeliverNotificationService
import dev.thiago.notification_service.domain.model.NotificationEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class NotificationConsumer(
    private val deliverNotification: DeliverNotificationService
) {

    private val log = LoggerFactory.getLogger(NotificationConsumer::class.java)

    @RabbitListener(queues = [RabbitMQConfig.QUEUE])
    fun consume(event: NotificationEvent) {
        log.info("Mensagem recebida: ${event.notificationId}")
        deliverNotification.deliver(event)
    }
}