package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.Notification
import dev.thiago.notification_service.domain.port.input.SendNotificationRequest
import dev.thiago.notification_service.domain.port.input.SendNotificationUseCase
import dev.thiago.notification_service.domain.port.output.NotificationPublisherPort
import dev.thiago.notification_service.domain.port.output.SaveNotificationPort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.*

@Service
class SendNotificationService(
    private val tenantRepository: TenantRepository,
    @Qualifier("notificationRepositoryAdapter")
    private val saveNotification: SaveNotificationPort,
    private val publishNotification: NotificationPublisherPort

) : SendNotificationUseCase {

    override fun send(apiKey: String, request: SendNotificationRequest): Notification {
        val tenant = tenantRepository.findByApiKey(apiKey)
            ?: throw IllegalArgumentException("Invalid API key")

        val notification = Notification(
            tenantId = tenant.id,
            templateId = request.templateId?.let { UUID.fromString(it) },
            groupId = request.groupId?.let { UUID.fromString(it) },
            payload = request.payload
        )

        val saved = saveNotification.save(notification)
        publishNotification.publish(saved)
        return saved

    }
}