package dev.thiago.notification_service.domain.port.output

import java.util.UUID

fun interface AddGroupMemberPort {
    fun add(groupId: UUID, recipientId: UUID)
}