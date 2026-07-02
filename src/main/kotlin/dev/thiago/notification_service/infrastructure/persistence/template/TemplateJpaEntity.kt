package dev.thiago.notification_service.infrastructure.persistence.template

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "templates")
class TemplateJpaEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "tenant_id", nullable = false)
    val tenantId: UUID,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val channel: String,

    val subject: String?,

    @Column(nullable = false, columnDefinition = "TEXT")
    val body: String,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)