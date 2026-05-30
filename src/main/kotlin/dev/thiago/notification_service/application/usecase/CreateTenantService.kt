package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.Tenant
import dev.thiago.notification_service.domain.port.input.CreateTenantUseCase
import dev.thiago.notification_service.domain.port.output.SaveTenantPort
import org.springframework.stereotype.Service
import java.util.*

@Service
class CreateTenantService(
    private val saveTenant: SaveTenantPort
) : CreateTenantUseCase {

    override fun create(name: String): Tenant {
        val tenant = Tenant(
            id = UUID.randomUUID(),
            name = name,
            apiKey = generateApiKey(),
            status = "ACTIVE"
        )
        return saveTenant.save(tenant)
    }

    private fun generateApiKey(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
}