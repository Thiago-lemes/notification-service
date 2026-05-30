package dev.thiago.notification_service.infrastructure.persistence.recipient

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "recipients")
class RecipientJpaEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "tenant_id", nullable = false)
    val tenantId: UUID,

    @Column(nullable = false)
    val name: String,

    val email: String?,

    val phone: String?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "channel_preferences", columnDefinition = "jsonb")
    val channelPreferences: List<String> = listOf("EMAIL"),

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)