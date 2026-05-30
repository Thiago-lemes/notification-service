package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.port.input.AddGroupMemberUseCase
import dev.thiago.notification_service.domain.port.output.AddGroupMemberPort
import dev.thiago.notification_service.domain.port.output.FindGroupPort
import dev.thiago.notification_service.domain.port.output.FindRecipientsByTenantPort
import org.springframework.stereotype.Service
import java.util.*

@Service
class AddGroupMemberService(
    private val findGroup: FindGroupPort,
    private val findRecipients: FindRecipientsByTenantPort,
    private val addGroupMember: AddGroupMemberPort
) : AddGroupMemberUseCase {

    override fun add(groupId: UUID, recipientId: UUID) {
        val group = findGroup.findById(groupId)
            ?: throw IllegalArgumentException("Group $groupId not found")

        val recipients = findRecipients.findByTenantId(group.tenantId)
        val recipientExists = recipients.any { it.id == recipientId }

        require(recipientExists) { "Recipient $recipientId not found in tenant" }

        addGroupMember.add(groupId, recipientId)
    }
}