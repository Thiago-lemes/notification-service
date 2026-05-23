package dev.thiago.notification_service.infrastructure.channel

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.domain.port.output.NotificationChannelPort
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class WhatsAppChannel(
    @Value($$"${twilio.account-sid}") private val accountSid: String,
    @Value($$"${twilio.auth-token}") private val authToken: String,
    @Value($$"${twilio.whatsapp-from}") private val from: String
) : NotificationChannelPort {

    private val log = LoggerFactory.getLogger(WhatsAppChannel::class.java)

    @PostConstruct
    fun init() {
        Twilio.init(accountSid, authToken)
        log.info("Twilio WhatsApp channel initialized")
    }

    override fun supports(channel: String) = channel == "WHATSAPP"

    override fun deliver(recipient: Recipient, payload: Map<String, Any>) {
        val phone = recipient.phone
            ?: throw IllegalStateException("Recipient ${recipient.id} has no phone number")

        val messageBody = payload["message"] as? String ?: ""

        val message = Message.creator(
            PhoneNumber("whatsapp:$phone"),
            PhoneNumber(from),
            messageBody
        ).create()

        log.info("WhatsApp enviado para $phone — SID: ${message.sid}")
    }
}