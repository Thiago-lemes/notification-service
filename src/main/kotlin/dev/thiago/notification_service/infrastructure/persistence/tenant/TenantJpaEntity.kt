package dev.thiago.notification_service.infrastructure.persistence.tenant

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

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