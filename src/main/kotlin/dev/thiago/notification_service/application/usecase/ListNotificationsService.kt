package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.port.input.ListNotificationsUseCase
import dev.thiago.notification_service.domain.port.input.NotificationPage
import dev.thiago.notification_service.domain.port.output.FindNotificationPort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class ListNotificationsService(
    private val tenantRepository: TenantRepository,
    private val findNotification: FindNotificationPort
) : ListNotificationsUseCase {

    override fun list(apiKey: String, page: Int, size: Int): NotificationPage {
        val tenant = tenantRepository.findByApiKey(apiKey)
            ?: throw IllegalArgumentException("Invalid API key")

        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val result = findNotification.findByTenantId(tenant.id, pageable)

        return NotificationPage(
            content = result.content,
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages
        )
    }
}