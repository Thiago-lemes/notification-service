package dev.thiago.notification_service.domain.port.input

import dev.thiago.notification_service.domain.model.Template

fun interface ListTemplatesUseCase {
    fun list(apiKey: String): List<Template>
}