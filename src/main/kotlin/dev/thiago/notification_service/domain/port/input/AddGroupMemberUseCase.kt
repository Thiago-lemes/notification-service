package dev.thiago.notification_service.domain.port.input

import java.util.*

fun interface AddGroupMemberUseCase {
    fun add(groupId: UUID, recipientId: UUID)
}