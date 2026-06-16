package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.RecipientGroup
import dev.thiago.notification_service.domain.port.input.ListGroupsUseCase
import dev.thiago.notification_service.domain.port.output.FindGroupsByTenantPort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import org.springframework.stereotype.Service

@Service
class ListGroupsService(
    private val tenantRepository: TenantRepository,
    private val findGroups: FindGroupsByTenantPort
) : ListGroupsUseCase {

    override fun list(apiKey: String): List<RecipientGroup> {
        val tenant = tenantRepository.findByApiKey(apiKey)
            ?: throw IllegalArgumentException("Invalid API key")
        return findGroups.findByTenantId(tenant.id)
    }
}