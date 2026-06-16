package dev.thiago.notification_service.domain.port.input

import dev.thiago.notification_service.domain.model.RecipientGroup

fun interface ListGroupsUseCase {
    fun list(apiKey: String): List<RecipientGroup>
}