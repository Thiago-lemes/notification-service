package dev.thiago.notification_service.domain.port.input

import dev.thiago.notification_service.domain.model.Recipient

fun interface CreateRecipientUseCase {
    fun create(request: CreateRecipientRequest): Recipient
}

data class CreateRecipientRequest(
    val tenantId: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val webhookUrl: String?,
    val channelPreferences: List<String>
)