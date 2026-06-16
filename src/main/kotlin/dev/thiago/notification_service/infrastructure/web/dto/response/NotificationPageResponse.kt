package dev.thiago.notification_service.infrastructure.web.dto.response

import dev.thiago.notification_service.domain.port.input.NotificationPage
import java.util.*

data class NotificationPageResponse(
    val content: List<NotificationItemResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
) {
    companion object {
        fun from(page: NotificationPage) = NotificationPageResponse(
            content = page.content.map { NotificationItemResponse.from(it) },
            page = page.page,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages
        )
    }
}

data class NotificationItemResponse(
    val id: UUID,
    val status: String,
    val templateId: UUID?,
    val groupId: UUID?,
    val payload: Map<String, Any>
) {
    companion object {
        fun from(n: dev.thiago.notification_service.domain.model.Notification) = NotificationItemResponse(
            id = n.id,
            status = n.status,
            templateId = n.templateId,
            groupId = n.groupId,
            payload = n.payload
        )
    }
}