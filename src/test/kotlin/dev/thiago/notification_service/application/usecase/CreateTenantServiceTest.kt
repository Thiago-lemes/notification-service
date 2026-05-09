package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.port.output.SaveTenantPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class CreateTenantServiceTest {

    private val saveTenant = mockk<SaveTenantPort>()
    private val service = CreateTenantService(saveTenant = saveTenant)

    @Test
    fun `should create tenant with generated api key`() {
        // ARRANGE
        every { saveTenant.save(any()) } answers { firstArg() }

        // ACT
        val result = service.create(name = "Escola Municipal Centro")

        // ASSERT
        assertNotNull(result.id)
        assertEquals("Escola Municipal Centro", result.name)
        assertEquals("ACTIVE", result.status)
        assertNotNull(result.apiKey)
    }

    @Test
    fun `should generate unique api keys for different tenants`() {
        // ARRANGE
        every { saveTenant.save(any()) } answers { firstArg() }

        // ACT
        val tenant1 = service.create(name = "Escola A")
        val tenant2 = service.create(name = "Escola B")

        // ASSERT — duas chamadas nunca devem gerar a mesma chave
        assert(tenant1.apiKey != tenant2.apiKey) {
            "API keys devem ser únicas por tenant"
        }
    }

    @Test
    fun `should generate api key with 32 characters`() {
        // ARRANGE
        every { saveTenant.save(any()) } answers { firstArg() }

        // ACT
        val result = service.create(name = "Escola Teste")

        // ASSERT — UUID sem hífens = 32 caracteres
        assertEquals(32, result.apiKey.length)
    }

    @Test
    fun `should persist tenant exactly once`() {
        // ARRANGE
        every { saveTenant.save(any()) } answers { firstArg() }

        // ACT
        service.create(name = "Escola Teste")

        // ASSERT
        verify(exactly = 1) { saveTenant.save(any()) }
    }

    @Test
    fun `should generate different ids for different tenants`() {
        // ARRANGE
        every { saveTenant.save(any()) } answers { firstArg() }

        // ACT
        val tenant1 = service.create(name = "Escola A")
        val tenant2 = service.create(name = "Escola B")

        // ASSERT
        assert(tenant1.id != tenant2.id) {
            "IDs devem ser únicos por tenant"
        }
    }
}