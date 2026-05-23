package dev.thiago.notification_service.infrastructure.messaging

import dev.thiago.notification_service.application.usecase.DeliverNotificationService
import dev.thiago.notification_service.domain.model.NotificationEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class NotificationConsumer(
    private val deliverNotification: DeliverNotificationService,
    private val rabbitTemplate: RabbitTemplate
) {

    private val log = LoggerFactory.getLogger(NotificationConsumer::class.java)

    private val retryQueues = listOf(
        "notification.retry.2s",
        "notification.retry.4s",
        "notification.retry.8s",
        "notification.retry.16s",
        "notification.retry.32s"
    )

    @RabbitListener(queues = [RabbitMQConfig.QUEUE])
    fun consume(message: Message) {
        val retryCount = getRetryCount(message)
        val event = deserialize(message)

        log.info("Mensagem recebida: ${event.notificationId} (tentativa ${retryCount + 1})")

        try {
            deliverNotification.deliver(event)
        } catch (e: Exception) {
            log.error("Falha ao processar mensagem: ${e.message}")
            handleFailure(message, event, retryCount)
        }
    }

    private fun handleFailure(message: Message, event: NotificationEvent, retryCount: Int) {
        if (retryCount >= RabbitMQConfig.MAX_RETRY_COUNT) {
            log.error("Mensagem ${event.notificationId} esgotou tentativas — enviando para DLQ")
            sendToDlq(message)
            return
        }

        val routingKey = retryQueues[retryCount]
        log.warn("Agendando retry ${retryCount + 1} para ${event.notificationId} via $routingKey")

        val properties = MessageProperties().apply {
            headers.putAll(message.messageProperties.headers)
            setHeader("x-retry-count", retryCount + 1)
        }

        val retryMessage = Message(message.body, properties)
        rabbitTemplate.send(RabbitMQConfig.EXCHANGE, routingKey, retryMessage)
    }

    private fun sendToDlq(message: Message) {
        rabbitTemplate.send(
            "${RabbitMQConfig.EXCHANGE}.dlx",
            RabbitMQConfig.DLQ_ROUTING_KEY,
            message
        )
    }

    private fun getRetryCount(message: Message): Int {
        return message.messageProperties.headers["x-retry-count"] as? Int ?: 0
    }

    private fun deserialize(message: Message): NotificationEvent {
        val converter = rabbitTemplate.messageConverter
        return converter.fromMessage(message) as NotificationEvent
    }
}