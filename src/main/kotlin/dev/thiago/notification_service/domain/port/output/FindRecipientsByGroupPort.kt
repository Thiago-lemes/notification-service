package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Recipient
import java.util.*

fun interface FindRecipientsByGroupPort {
    fun findByGroupId(groupId: UUID): List<Recipient>
}