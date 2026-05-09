package dev.thiago.notification_service.infrastructure.persistence.notificationDelivery

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationDeliveryJpaRepository : JpaRepository<NotificationDeliveryJpaEntity, UUID>