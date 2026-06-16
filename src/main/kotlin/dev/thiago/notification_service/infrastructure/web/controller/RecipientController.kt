package dev.thiago.notification_service.infrastructure.web.controller

import dev.thiago.notification_service.domain.port.input.CreateRecipientRequest
import dev.thiago.notification_service.domain.port.input.CreateRecipientUseCase
import dev.thiago.notification_service.infrastructure.web.dto.response.RecipientResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import dev.thiago.notification_service.infrastructure.web.dto.request.CreateRecipientRequest as CreateRecipientRequestDto

@RestController
@RequestMapping("/recipients")
class RecipientController(
    private val createRecipient: CreateRecipientUseCase
) {

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateRecipientRequestDto
    ): ResponseEntity<RecipientResponse> {
        val recipient = createRecipient.create(
            CreateRecipientRequest(
                tenantId = request.tenantId,
                name = request.name,
                email = request.email,
                phone = request.phone,
                channelPreferences = request.channelPreferences
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(RecipientResponse.from(recipient))
    }
}