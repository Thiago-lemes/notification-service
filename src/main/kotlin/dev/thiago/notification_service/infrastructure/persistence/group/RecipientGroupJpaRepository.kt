package dev.thiago.notification_service.infrastructure.persistence.group

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecipientGroupJpaRepository : JpaRepository<RecipientGroupJpaEntity, UUID>