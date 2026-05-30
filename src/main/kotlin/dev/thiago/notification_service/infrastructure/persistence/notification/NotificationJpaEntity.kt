package dev.thiago.notification_service.infrastructure.persistence.notification

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "notifications")
class NotificationJpaEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "tenant_id", nullable = false)
    val tenantId: UUID,

    @Column(name = "template_id")
    val templateId: UUID?,

    @Column(name = "group_id")
    val groupId: UUID?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val payload: Map<String, Any>,

    @Column(nullable = false)
    val status: String = "PENDING",

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)