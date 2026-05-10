package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Recipient

fun interface SaveRecipientPort {
    fun save(recipient: Recipient): Recipient
}