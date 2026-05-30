package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.RecipientGroup
import java.util.*

fun interface FindGroupPort {
    fun findById(id: UUID): RecipientGroup?
}