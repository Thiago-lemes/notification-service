package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.NotificationDelivery
import dev.thiago.notification_service.domain.model.NotificationEvent
import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.domain.port.output.*
import dev.thiago.notification_service.domain.service.TemplateRenderer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class DeliverNotificationService(
    private val findNotification: FindNotificationPort,
    private val findRecipients: FindRecipientsByTenantPort,
    private val findRecipientsByGroup: FindRecipientsByGroupPort,
    private val findTemplate: FindTemplatePort,
    private val saveDelivery: SaveDeliveryPort,
    private val channels: List<NotificationChannelPort>
) {

    private val log = LoggerFactory.getLogger(DeliverNotificationService::class.java)

    fun deliver(event: NotificationEvent) {
        // 1. busca a notificação
        val notification = findNotification.findById(event.notificationId)
            ?: throw IllegalArgumentException("Notification ${event.notificationId} not found")

        // 2. resolve os destinatários — por grupo ou por tenant
        val recipients = resolveRecipients(notification.groupId, notification.tenantId)

        if (recipients.isEmpty()) {
            log.warn("Nenhum destinatário encontrado para notificação ${notification.id}")
            return
        }

        // 3. busca o template se existir
        val template = notification.templateId?.let {
            findTemplate.findById(it)
        }

        // 4. para cada destinatário, para cada canal preferido
        recipients.forEach { recipient ->
            val preferredChannels = recipient.channelPreferences
            preferredChannels.forEach { channelName ->
                val channel = channels.find { it.supports(channelName) }

                if (channel == null) {
                    log.warn("Nenhum canal encontrado para $channelName")
                    return@forEach
                }

                val payload = if (template != null && channelName != "WEBHOOK") {
                    val enrichedPayload = notification.payload + mapOf(
                        "nome" to recipient.name,
                        "email" to (recipient.email ?: ""),
                        "phone" to (recipient.phone ?: "")
                    )
                    val rendered = TemplateRenderer.render(template, enrichedPayload)
                    mapOf(
                        "subject" to (rendered.subject ?: ""),
                        "message" to rendered.body
                    )
                } else {
                    notification.payload
                }

                val delivery = NotificationDelivery(
                    notificationId = notification.id,
                    recipientId = recipient.id,
                    channel = channelName,
                    status = "PENDING"
                )

                val saved = saveDelivery.save(delivery)

                try {
                    channel.deliver(recipient, payload)
                    saveDelivery.save(saved.copy(status = "DELIVERED", attemptCount = 1))
                    log.info("Entregue via $channelName para ${recipient.name}")
                } catch (e: Exception) {
                    saveDelivery.save(
                        saved.copy(
                            status = "FAILED",
                            attemptCount = 1,
                            errorMessage = e.message
                        )
                    )
                    log.error("Falha ao entregar via $channelName para ${recipient.name}: ${e.message}")
                    throw RuntimeException("One or more deliveries failed — triggering retry")
                }
            }
        }
    }

    private fun resolveRecipients(groupId: UUID?, tenantId: UUID): List<Recipient> {
        return if (groupId != null) {
            log.info("Buscando recipients do grupo $groupId")
            findRecipientsByGroup.findByGroupId(groupId)
        } else {
            log.info("Buscando todos os recipients do tenant $tenantId")
            findRecipients.findByTenantId(tenantId)
        }
    }
}