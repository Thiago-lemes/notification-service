package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.domain.port.input.CreateRecipientRequest
import dev.thiago.notification_service.domain.port.input.CreateRecipientUseCase
import dev.thiago.notification_service.domain.port.output.SaveRecipientPort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CreateRecipientService(
    private val tenantRepository: TenantRepository,
    private val saveRecipient: SaveRecipientPort
) : CreateRecipientUseCase {

    override fun create(request: CreateRecipientRequest): Recipient {
        val tenant = tenantRepository.findById(UUID.fromString(request.tenantId))
            ?: throw IllegalArgumentException("Tenant ${request.tenantId} not found")

        require(request.email != null || request.phone != null) {
            "Recipient must have at least one contact — email or phone"
        }

        require(request.channelPreferences.isNotEmpty()) {
            "Recipient must have at least one channel preference"
        }

        val recipient = Recipient(
            id = UUID.randomUUID(),
            tenantId = tenant.id,
            name = request.name,
            email = request.email,
            phone = request.phone,
            channelPreferences = request.channelPreferences
        )

        return saveRecipient.save(recipient)
    }
}