package dev.thiago.notification_service.domain.port.input

import dev.thiago.notification_service.domain.model.Recipient

fun interface ListRecipientsUseCase {
    fun list(apiKey: String): List<Recipient>
}