package dev.thiago.notification_service.infrastructure.persistence.tenant

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tenants")
class TenantJpaEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val name: String,

    @Column(name = "api_key", nullable = false, unique = true)
    val apiKey: String,

    @Column(nullable = false)
    val status: String = "ACTIVE",

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)