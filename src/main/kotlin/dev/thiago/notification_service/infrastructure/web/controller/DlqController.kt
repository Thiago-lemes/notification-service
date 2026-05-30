package dev.thiago.notification_service.infrastructure.web.controller

import dev.thiago.notification_service.infrastructure.messaging.RabbitMQConfig
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/dlq")
class DlqController(
    private val rabbitTemplate: RabbitTemplate
) {

    private val log = LoggerFactory.getLogger(DlqController::class.java)

    @GetMapping("/messages")
    fun listMessages(): ResponseEntity<Any> {
        val messages = mutableListOf<Map<String, Any?>>()
        val tempMessages = mutableListOf<Message>()

        // verifica quantas mensagens tem na fila
        val props = rabbitTemplate.execute { channel ->
            channel.queueDeclarePassive(RabbitMQConfig.DLQ)
        }

        val messageCount = props?.messageCount ?: 0

        repeat(messageCount) {
            val message = rabbitTemplate.receive(RabbitMQConfig.DLQ) ?: return@repeat
            tempMessages.add(message)

            messages.add(
                mapOf(
                    "notificationId" to extractNotificationId(message),
                    "retryCount" to message.messageProperties.headers["x-retry-count"]
                )
            )
        }

        // devolve todas para a DLQ
        tempMessages.forEach { message ->
            rabbitTemplate.send(
                "${RabbitMQConfig.EXCHANGE}.dlx",
                RabbitMQConfig.DLQ_ROUTING_KEY,
                message
            )
        }

        return ResponseEntity.ok(
            mapOf(
                "total" to messages.size,
                "messages" to messages
            )
        )
    }

    @PostMapping("/reprocess")
    fun reprocess(): ResponseEntity<Any> {
        var count = 0

        val props = rabbitTemplate.execute { channel ->
            channel.queueDeclarePassive(RabbitMQConfig.DLQ)
        }

        val messageCount = props?.messageCount ?: 0

        repeat(messageCount) {
            val message = rabbitTemplate.receive(RabbitMQConfig.DLQ) ?: return@repeat

            val properties = message.messageProperties
            properties.headers.remove("x-retry-count")

            val cleanMessage = Message(message.body, properties)
            rabbitTemplate.send(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, cleanMessage)
            count++

            log.info("Mensagem reprocessada da DLQ: ${extractNotificationId(message)}")
        }

        return ResponseEntity.ok(
            mapOf(
                "message" to "Reprocessed $count messages from DLQ",
                "count" to count
            )
        )
    }

    private fun extractNotificationId(message: Message): String? {
        return try {
            val body = String(message.body)
            val match = Regex("\"notificationId\"\\s*:\\s*\"([^\"]+)\"").find(body)
            match?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}