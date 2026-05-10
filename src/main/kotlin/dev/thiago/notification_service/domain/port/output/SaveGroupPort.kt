package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.RecipientGroup

fun interface SaveGroupPort {
    fun save(group: RecipientGroup): RecipientGroup
}