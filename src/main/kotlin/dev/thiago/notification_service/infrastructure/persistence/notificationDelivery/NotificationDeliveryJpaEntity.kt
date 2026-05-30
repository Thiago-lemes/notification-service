package dev.thiago.notification_service.infrastructure.persistence.notificationDelivery

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "notification_deliveries")
class NotificationDeliveryJpaEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "notification_id", nullable = false)
    val notificationId: UUID,

    @Column(name = "recipient_id", nullable = false)
    val recipientId: UUID,

    @Column(nullable = false)
    val channel: String,

    @Column(nullable = false)
    val status: String = "PENDING",

    @Column(name = "attempt_count")
    val attemptCount: Int = 0,

    @Column(name = "last_attempt_at")
    val lastAttemptAt: LocalDateTime? = null,

    @Column(name = "error_message")
    val errorMessage: String? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)