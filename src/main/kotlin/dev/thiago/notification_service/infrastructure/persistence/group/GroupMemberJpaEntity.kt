package dev.thiago.notification_service.infrastructure.persistence.group

import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.util.*

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