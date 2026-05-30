package dev.thiago.notification_service.infrastructure.persistence.group

import dev.thiago.notification_service.domain.model.RecipientGroup
import dev.thiago.notification_service.domain.port.output.AddGroupMemberPort
import dev.thiago.notification_service.domain.port.output.FindGroupPort
import dev.thiago.notification_service.domain.port.output.SaveGroupPort
import org.springframework.stereotype.Component
import java.util.*

@Component
class GroupRepositoryAdapter(
    private val groupJpaRepository: RecipientGroupJpaRepository,
    private val groupMemberJpaRepository: GroupMemberJpaRepository
) : SaveGroupPort, FindGroupPort, AddGroupMemberPort {

    override fun save(group: RecipientGroup): RecipientGroup {
        groupJpaRepository.save(group.toEntity())
        return group
    }

    override fun findById(id: UUID): RecipientGroup? {
        return groupJpaRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun add(groupId: UUID, recipientId: UUID) {
        val member = GroupMemberJpaEntity(
            id = GroupMemberId(groupId = groupId, recipientId = recipientId)
        )
        groupMemberJpaRepository.save(member)
    }

    private fun RecipientGroup.toEntity() = RecipientGroupJpaEntity(
        id = id,
        tenantId = tenantId,
        name = name,
        description = description
    )

    private fun RecipientGroupJpaEntity.toDomain() = RecipientGroup(
        id = id,
        tenantId = tenantId,
        name = name,
        description = description
    )
}