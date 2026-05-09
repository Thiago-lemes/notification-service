package dev.thiago.notification_service.infrastructure.channel

import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.domain.port.output.NotificationChannelPort
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class EmailChannel(
    private val mailSender: JavaMailSender
) : NotificationChannelPort {

    private val log = LoggerFactory.getLogger(EmailChannel::class.java)

    override fun supports(channel: String) = channel == "EMAIL"

    override fun deliver(recipient: Recipient, payload: Map<String, Any>) {
        val email = recipient.email
            ?: throw IllegalStateException("Recipient ${recipient.id} has no email")

        val message = SimpleMailMessage().apply {
            setTo(email)
            subject = payload["subject"] as? String ?: "Notificação"
            text = payload["message"] as? String ?: ""
        }

        mailSender.send(message)
        log.info("Email enviado para ${recipient.email}")
    }
}