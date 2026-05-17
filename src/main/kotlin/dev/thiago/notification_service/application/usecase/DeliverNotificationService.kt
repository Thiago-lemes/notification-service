package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.NotificationDelivery
import dev.thiago.notification_service.domain.model.NotificationEvent
import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.domain.port.output.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DeliverNotificationService(
    private val findNotification: FindNotificationPort,
    private val findRecipients: FindRecipientsByTenantPort,
    private val findRecipientsByGroup: FindRecipientsByGroupPort,
    private val saveDelivery: SaveDeliveryPort,
    private val channels: List<NotificationChannelPort>
) {

    private val log = LoggerFactory.getLogger(DeliverNotificationService::class.java)

    fun deliver(event: NotificationEvent) {
        val notification = findNotification.findById(event.notificationId)
            ?: throw IllegalArgumentException("Notification ${event.notificationId} not found")

        val recipients = resolveRecipients(notification.groupId, notification.tenantId)

        if (recipients.isEmpty()) {
            log.warn("Nenhum destinatário encontrado para notificação ${notification.id}")
            return
        }

        recipients.forEach { recipient ->
            recipient.channelPreferences.forEach { channelName ->
                val channel = channels.find { it.supports(channelName) }

                if (channel == null) {
                    log.warn("Nenhum canal encontrado para $channelName")
                    return@forEach
                }

                val delivery = NotificationDelivery(
                    notificationId = notification.id,
                    recipientId = recipient.id,
                    channel = channelName,
                    status = "PENDING"
                )

                val saved = saveDelivery.save(delivery)

                try {
                    channel.deliver(recipient, notification.payload)
                    saveDelivery.save(saved.copy(status = "DELIVERED", attemptCount = 1))
                    log.info("Entregue via $channelName para ${recipient.name}")
                } catch (e: Exception) {
                    saveDelivery.save(saved.copy(
                        status = "FAILED",
                        attemptCount = 1,
                        errorMessage = e.message
                    ))
                    log.error("Falha ao entregar via $channelName para ${recipient.name}: ${e.message}")
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