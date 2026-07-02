package dev.thiago.notification_service.infrastructure.web.dto.response

import dev.thiago.notification_service.domain.model.Template
import java.util.*

data class TemplateResponse(
    val id: UUID,
    val tenantId: UUID,
    val name: String,
    val channel: String,
    val subject: String?,
    val body: String
) {
    companion object {
        fun from(template: Template) = TemplateResponse(
            id = template.id,
            tenantId = template.tenantId,
            name = template.name,
            channel = template.channel,
            subject = template.subject,
            body = template.body
        )
    }
}