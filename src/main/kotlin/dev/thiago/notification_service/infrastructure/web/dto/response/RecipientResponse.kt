package dev.thiago.notification_service.infrastructure.web.dto.response

import dev.thiago.notification_service.domain.model.Recipient
import java.util.*

data class RecipientResponse(
    val id: UUID,
    val tenantId: UUID,
    val name: String,
    val email: String?,
    val phone: String?,
    val channelPreferences: List<String>
) {
    companion object {
        fun from(recipient: Recipient) = RecipientResponse(
            id = recipient.id,
            tenantId = recipient.tenantId,
            name = recipient.name,
            email = recipient.email,
            phone = recipient.phone,
            channelPreferences = recipient.channelPreferences
        )
    }
}