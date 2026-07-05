package dev.thiago.notification_service.infrastructure.persistence

import dev.thiago.notification_service.infrastructure.persistence.tenant.TenantJpaEntity
import dev.thiago.notification_service.infrastructure.persistence.tenant.TenantJpaRepository
import dev.thiago.notification_service.infrastructure.persistence.tenant.TenantRepositoryAdapter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class TenantRepositoryAdapterTest(
    @Autowired private val jpaRepository: TenantJpaRepository
) : RepositoryIntegrationTest() {

    private val adapter = TenantRepositoryAdapter(jpaRepository)

    @Test
    fun `should find tenant by api key when it exists`() {
        // ARRANGE
        val entity = TenantJpaEntity(
            id = UUID.randomUUID(),
            name = "Escola Teste",
            apiKey = "test-api-key-001",
            status = "ACTIVE"
        )
        jpaRepository.save(entity)

        // ACT
        val result = adapter.findByApiKey("test-api-key-001")

        // ASSERT
        assertNotNull(result)
        assertEquals("Escola Teste", result!!.name)
        assertEquals("test-api-key-001", result.apiKey)
    }

    @Test
    fun `should return null when api key does not exist`() {
        // ACT
        val result = adapter.findByApiKey("chave-que-nao-existe")

        // ASSERT
        assertNull(result)
    }

    @Test
    fun `should save tenant and retrieve by id`() {
        // ARRANGE
        val entity = TenantJpaEntity(
            id = UUID.randomUUID(),
            name = "Cantina Teste",
            apiKey = "cantina-key-001",
            status = "ACTIVE"
        )
        jpaRepository.save(entity)

        // ACT
        val result = adapter.findById(entity.id)

        // ASSERT
        assertNotNull(result)
        assertEquals("Cantina Teste", result!!.name)
    }

    @Test
    fun `should return null when tenant id does not exist`() {
        // ACT
        val result = adapter.findById(UUID.randomUUID())

        // ASSERT
        assertNull(result)
    }
}