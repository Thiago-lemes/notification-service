package dev.thiago.notification_service.infrastructure.channel

import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.domain.port.output.NotificationChannelPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class WhatsAppChannel : NotificationChannelPort {

    private val log = LoggerFactory.getLogger(WhatsAppChannel::class.java)

    override fun supports(channel: String) = channel == "WHATSAPP"

    override fun deliver(recipient: Recipient, payload: Map<String, Any>) {
        val phone = recipient.phone
            ?: throw IllegalStateException("Recipient ${recipient.id} has no phone number")

        val message = payload["message"] as? String ?: ""

        // TODO: integrar com Twilio ou Meta WhatsApp Business API
        log.info("📱 [WHATSAPP MOCK] Para: $phone | Mensagem: $message")
    }
}