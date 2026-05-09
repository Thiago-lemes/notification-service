package dev.thiago.notification_service.infrastructure.persistence.notification

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationJpaRepository : JpaRepository<NotificationJpaEntity, UUID>