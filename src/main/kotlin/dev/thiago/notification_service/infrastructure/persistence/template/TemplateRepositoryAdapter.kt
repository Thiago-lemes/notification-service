package dev.thiago.notification_service.infrastructure.persistence.template

import dev.thiago.notification_service.domain.model.Template
import dev.thiago.notification_service.domain.port.output.FindTemplatePort
import dev.thiago.notification_service.domain.port.output.SaveTemplatePort
import org.springframework.stereotype.Component
import java.util.*

@Component
class TemplateRepositoryAdapter(
    private val jpaRepository: TemplateJpaRepository
) : SaveTemplatePort, FindTemplatePort {

    override fun save(template: Template): Template {
        jpaRepository.save(template.toEntity())
        return template
    }

    override fun findById(id: UUID): Template? {
        return jpaRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findByTenantId(tenantId: UUID): List<Template> {
        return jpaRepository.findByTenantId(tenantId).map { it.toDomain() }
    }

    private fun Template.toEntity() = TemplateJpaEntity(
        id = id,
        tenantId = tenantId,
        name = name,
        channel = channel,
        subject = subject,
        body = body
    )

    private fun TemplateJpaEntity.toDomain() = Template(
        id = id,
        tenantId = tenantId,
        name = name,
        channel = channel,
        subject = subject,
        body = body
    )
}