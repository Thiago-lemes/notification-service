package dev.thiago.notification_service.infrastructure.persistence.tenant

import dev.thiago.notification_service.domain.model.Tenant
import dev.thiago.notification_service.domain.port.output.SaveTenantPort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import org.springframework.stereotype.Component

@Component
class TenantRepositoryAdapter(
    private val jpaRepository: TenantJpaRepository
) : TenantRepository, SaveTenantPort {

    override fun findByApiKey(apiKey: String): Tenant? {
        return jpaRepository.findByApiKey(apiKey)?.toDomain()
    }

    override fun save(tenant: Tenant): Tenant {
        jpaRepository.save(tenant.toEntity())
        return tenant
    }

    private fun TenantJpaEntity.toDomain() = Tenant(
        id = id,
        name = name,
        apiKey = apiKey,
        status = status
    )

    private fun Tenant.toEntity() = TenantJpaEntity(
        id = id,
        name = name,
        apiKey = apiKey,
        status = status
    )
}