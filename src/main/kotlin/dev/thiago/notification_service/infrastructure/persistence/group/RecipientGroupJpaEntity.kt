package dev.thiago.notification_service.infrastructure.persistence.group

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "recipient_groups")
class RecipientGroupJpaEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "tenant_id", nullable = false)
    val tenantId: UUID,

    @Column(nullable = false)
    val name: String,

    val description: String?,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)