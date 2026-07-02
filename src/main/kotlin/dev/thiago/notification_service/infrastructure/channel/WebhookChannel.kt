package dev.thiago.notification_service.infrastructure.channel

import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.domain.port.output.NotificationChannelPort
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class WebhookChannel(
    private val restTemplate: RestTemplate
) : NotificationChannelPort {

    private val log = LoggerFactory.getLogger(WebhookChannel::class.java)

    override fun supports(channel: String) = channel == "WEBHOOK"

    override fun deliver(recipient: Recipient, payload: Map<String, Any>) {
        val url = recipient.webhookUrl
            ?: throw IllegalStateException("Recipient ${recipient.id} has no webhook URL")

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        val body = mapOf(
            "recipientId" to recipient.id,
            "recipientName" to recipient.name,
            "payload" to payload
        )

        val request = HttpEntity(body, headers)

        try {
            val response = restTemplate.postForEntity(url, request, String::class.java)
            log.info("Webhook entregue para $url — status: ${response.statusCode}")
        } catch (e: Exception) {
            log.error("Falha ao chamar webhook $url: ${e.message}")
            throw e
        }
    }
}