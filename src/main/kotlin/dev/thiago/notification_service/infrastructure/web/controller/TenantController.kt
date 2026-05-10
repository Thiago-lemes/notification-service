package dev.thiago.notification_service.infrastructure.web.controller

import dev.thiago.notification_service.domain.port.input.CreateTenantUseCase
import dev.thiago.notification_service.infrastructure.web.dto.request.CreateTenantRequest
import dev.thiago.notification_service.infrastructure.web.dto.response.CreateTenantResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tenants")
class TenantController(
    private val createTenant: CreateTenantUseCase
) {

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateTenantRequest
    ): ResponseEntity<CreateTenantResponse> {
        val tenant = createTenant.create(request.name)

        val response = CreateTenantResponse(
            id = tenant.id,
            name = tenant.name,
            apiKey = tenant.apiKey,
            status = tenant.status
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}