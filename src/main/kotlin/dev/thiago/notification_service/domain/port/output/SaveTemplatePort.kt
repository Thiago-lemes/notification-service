package dev.thiago.notification_service.domain.port.output

import dev.thiago.notification_service.domain.model.Template

fun interface SaveTemplatePort {
    fun save(template: Template): Template
}