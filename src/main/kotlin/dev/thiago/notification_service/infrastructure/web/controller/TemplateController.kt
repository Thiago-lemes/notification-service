package dev.thiago.notification_service.infrastructure.web.controller

import dev.thiago.notification_service.domain.port.input.CreateTemplateUseCase
import dev.thiago.notification_service.domain.port.input.ListTemplatesUseCase
import dev.thiago.notification_service.infrastructure.web.dto.request.CreateTemplateRequest
import dev.thiago.notification_service.infrastructure.web.dto.response.TemplateResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import dev.thiago.notification_service.domain.port.input.CreateTemplateRequest as CreateTemplateCommand

@RestController
@RequestMapping("/templates")
class TemplateController(
    private val createTemplate: CreateTemplateUseCase,
    private val listTemplates: ListTemplatesUseCase
) {

    @GetMapping
    fun list(
        @RequestHeader("X-API-Key") apiKey: String
    ): ResponseEntity<List<TemplateResponse>> {
        val templates = listTemplates.list(apiKey)
        return ResponseEntity.ok(templates.map { TemplateResponse.from(it) })
    }

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateTemplateRequest
    ): ResponseEntity<TemplateResponse> {
        val template = createTemplate.create(
            CreateTemplateCommand(
                tenantId = request.tenantId,
                name = request.name,
                channel = request.channel,
                subject = request.subject,
                body = request.body
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(TemplateResponse.from(template))
    }
}