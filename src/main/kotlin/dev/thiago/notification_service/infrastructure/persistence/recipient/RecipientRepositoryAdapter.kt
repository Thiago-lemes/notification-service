package dev.thiago.notification_service.infrastructure.persistence.recipient

import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.domain.port.output.FindRecipientsByTenantPort
import dev.thiago.notification_service.domain.port.output.SaveRecipientPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RecipientRepositoryAdapter(
    private val jpaRepository: RecipientJpaRepository
) : FindRecipientsByTenantPort, SaveRecipientPort {

    override fun findByTenantId(tenantId: UUID): List<Recipient> {
        return jpaRepository.findByTenantId(tenantId).map { it.toDomain() }
    }

    override fun save(recipient: Recipient): Recipient {
        jpaRepository.save(recipient.toEntity())
        return recipient
    }

    private fun RecipientJpaEntity.toDomain() = Recipient(
        id = id,
        tenantId = tenantId,
        name = name,
        email = email,
        phone = phone,
        channelPreferences = channelPreferences
    )

    private fun Recipient.toEntity() = RecipientJpaEntity(
        id = id,
        tenantId = tenantId,
        name = name,
        email = email,
        phone = phone,
        channelPreferences = channelPreferences
    )
}