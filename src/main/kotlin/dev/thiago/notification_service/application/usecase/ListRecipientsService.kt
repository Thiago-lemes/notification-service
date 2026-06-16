package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.domain.port.input.ListRecipientsUseCase
import dev.thiago.notification_service.domain.port.output.FindRecipientsByTenantPort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import org.springframework.stereotype.Service

@Service
class ListRecipientsService(
    private val tenantRepository: TenantRepository,
    private val findRecipients: FindRecipientsByTenantPort
) : ListRecipientsUseCase {

    override fun list(apiKey: String): List<Recipient> {
        val tenant = tenantRepository.findByApiKey(apiKey)
            ?: throw IllegalArgumentException("Invalid API key")
        return findRecipients.findByTenantId(tenant.id)
    }
}