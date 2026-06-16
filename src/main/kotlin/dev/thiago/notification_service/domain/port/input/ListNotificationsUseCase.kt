package dev.thiago.notification_service.domain.port.input

import dev.thiago.notification_service.domain.model.Notification

fun interface ListNotificationsUseCase {
    fun list(apiKey: String, page: Int, size: Int): NotificationPage
}

data class NotificationPage(
    val content: List<Notification>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)