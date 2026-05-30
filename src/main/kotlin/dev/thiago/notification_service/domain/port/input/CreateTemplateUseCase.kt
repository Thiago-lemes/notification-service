package dev.thiago.notification_service.domain.port.input

import dev.thiago.notification_service.domain.model.Template

fun interface CreateTemplateUseCase {
    fun create(request: CreateTemplateRequest): Template
}

data class CreateTemplateRequest(
    val tenantId: String,
    val name: String,
    val channel: String,
    val subject: String?,
    val body: String
)