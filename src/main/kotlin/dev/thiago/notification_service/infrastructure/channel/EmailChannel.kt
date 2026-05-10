package dev.thiago.notification_service.infrastructure.channel

import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.domain.port.output.NotificationChannelPort
import jakarta.mail.internet.InternetAddress
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class EmailChannel(
    private val mailSender: JavaMailSender,
    @Value("\${spring.mail.username}") private val from: String
) : NotificationChannelPort {

    private val log = LoggerFactory.getLogger(EmailChannel::class.java)

    override fun supports(channel: String) = channel == "EMAIL"

    override fun deliver(recipient: Recipient, payload: Map<String, Any>) {
        val email = recipient.email
            ?: throw IllegalStateException("Recipient ${recipient.id} has no email")

        val mimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, false, "UTF-8")

        helper.setFrom(InternetAddress(from, "Notification Service"))
        helper.setTo(email)
        helper.setSubject(payload["subject"] as? String ?: "Notificação")
        helper.setText(payload["message"] as? String ?: "")

        mailSender.send(mimeMessage)
        log.info("Email enviado para $email")
    }
}