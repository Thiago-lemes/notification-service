package dev.thiago.notification_service.infrastructure.persistence.group

import jakarta.persistence.*
import java.io.Serializable
import java.util.UUID

@Embeddable
data class GroupMemberId(
    val groupId: UUID,
    val recipientId: UUID
) : Serializable

@Entity
@Table(name = "group_members")
class GroupMemberJpaEntity(
    @EmbeddedId
    val id: GroupMemberId
)