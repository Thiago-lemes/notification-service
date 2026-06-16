package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.Template
import dev.thiago.notification_service.domain.port.input.ListTemplatesUseCase
import dev.thiago.notification_service.domain.port.output.FindTemplatePort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import org.springframework.stereotype.Service

@Service
class ListTemplatesService(
    private val tenantRepository: TenantRepository,
    private val findTemplate: FindTemplatePort
) : ListTemplatesUseCase {

    override fun list(apiKey: String): List<Template> {
        val tenant = tenantRepository.findByApiKey(apiKey)
            ?: throw IllegalArgumentException("Invalid API key")
        return findTemplate.findByTenantId(tenant.id)
    }
}