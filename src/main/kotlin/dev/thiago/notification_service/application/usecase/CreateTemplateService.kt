package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.Template
import dev.thiago.notification_service.domain.port.input.CreateTemplateRequest
import dev.thiago.notification_service.domain.port.input.CreateTemplateUseCase
import dev.thiago.notification_service.domain.port.output.SaveTemplatePort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class CreateTemplateService(
    private val tenantRepository: TenantRepository,
    private val saveTemplate: SaveTemplatePort
) : CreateTemplateUseCase {

    override fun create(request: CreateTemplateRequest): Template {
        val tenant = tenantRepository.findById(UUID.fromString(request.tenantId))
            ?: throw IllegalArgumentException("Tenant ${request.tenantId} not found")

        require(request.body.isNotBlank()) { "Template body cannot be empty" }
        require(request.channel in listOf("EMAIL", "WHATSAPP", "WEBHOOK")) {
            "Invalid channel: ${request.channel}"
        }

        val template = Template(
            tenantId = tenant.id,
            name = request.name,
            channel = request.channel,
            subject = request.subject,
            body = request.body
        )

        return saveTemplate.save(template)
    }
}