package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Template
import java.util.*

interface FindTemplatePort {
    fun findById(id: UUID): Template?
    fun findByTenantId(tenantId: UUID): List<Template>
}