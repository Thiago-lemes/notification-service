package dev.thiago.notification_service.infrastructure.persistence.recipient

import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.domain.port.output.FindRecipientsByTenantPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RecipientRepositoryAdapter(
    private val jpaRepository: RecipientJpaRepository
) : FindRecipientsByTenantPort {

    override fun findByTenantId(tenantId: UUID): List<Recipient> {
        return jpaRepository.findByTenantId(tenantId).map { it.toDomain() }
    }

    private fun RecipientJpaEntity.toDomain() = Recipient(
        id = id,
        tenantId = tenantId,
        name = name,
        email = email,
        phone = phone,
        channelPreferences = channelPreferences
    )
}