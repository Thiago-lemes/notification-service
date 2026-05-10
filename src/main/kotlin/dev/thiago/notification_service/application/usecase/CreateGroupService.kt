package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.RecipientGroup
import dev.thiago.notification_service.domain.port.input.CreateGroupRequest
import dev.thiago.notification_service.domain.port.input.CreateGroupUseCase
import dev.thiago.notification_service.domain.port.output.SaveGroupPort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CreateGroupService(
    private val tenantRepository: TenantRepository,
    private val saveGroup: SaveGroupPort
) : CreateGroupUseCase {

    override fun create(request: CreateGroupRequest): RecipientGroup {
        val tenant = tenantRepository.findById(UUID.fromString(request.tenantId))
            ?: throw IllegalArgumentException("Tenant ${request.tenantId} not found")

        val group = RecipientGroup(
            tenantId = tenant.id,
            name = request.name,
            description = request.description
        )

        return saveGroup.save(group)
    }
}