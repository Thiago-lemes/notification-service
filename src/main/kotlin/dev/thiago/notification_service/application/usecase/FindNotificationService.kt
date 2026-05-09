package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.Notification
import dev.thiago.notification_service.domain.port.input.FindNotificationUseCase
import dev.thiago.notification_service.domain.port.output.FindNotificationPort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FindNotificationService(
    private val tenantRepository: TenantRepository,
    private val findNotification: FindNotificationPort
) : FindNotificationUseCase {

    override fun find(apiKey: String, notificationId: UUID): Notification {
        val tenant = tenantRepository.findByApiKey(apiKey)
            ?: throw IllegalArgumentException("Invalid API key")

        val notification = findNotification.findById(notificationId)
            ?: throw NoSuchElementException("Notification $notificationId not found")

        if (notification.tenantId != tenant.id) {
            throw IllegalAccessException("Notification does not belong to this tenant")
        }

        return notification
    }
}