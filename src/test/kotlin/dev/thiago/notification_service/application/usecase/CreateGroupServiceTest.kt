package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.Tenant
import dev.thiago.notification_service.domain.port.input.CreateGroupRequest
import dev.thiago.notification_service.domain.port.output.SaveGroupPort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class CreateGroupServiceTest {

    private val tenantRepository = mockk<TenantRepository>()
    private val saveGroup = mockk<SaveGroupPort>()

    private val service = CreateGroupService(
        tenantRepository = tenantRepository,
        saveGroup = saveGroup
    )

    @Test
    fun `should create group when request is valid`() {
        // ARRANGE
        val tenant = buildTenant()
        every { tenantRepository.findById(tenant.id) } returns tenant
        every { saveGroup.save(any()) } answers { firstArg() }

        // ACT
        val result = service.create(
            CreateGroupRequest(
                tenantId = tenant.id.toString(),
                name = "Equipe de Produção",
                description = "Voluntários da cozinha"
            )
        )

        // ASSERT
        assertNotNull(result.id)
        assertEquals("Equipe de Produção", result.name)
        assertEquals("Voluntários da cozinha", result.description)
        assertEquals(tenant.id, result.tenantId)
        verify(exactly = 1) { saveGroup.save(any()) }
    }

    @Test
    fun `should create group without description`() {
        // ARRANGE
        val tenant = buildTenant()
        every { tenantRepository.findById(tenant.id) } returns tenant
        every { saveGroup.save(any()) } answers { firstArg() }

        // ACT
        val result = service.create(
            CreateGroupRequest(
                tenantId = tenant.id.toString(),
                name = "Equipe de Produção",
                description = null
            )
        )

        // ASSERT
        assertEquals(null, result.description)
    }

    @Test
    fun `should throw exception when tenant not found`() {
        // ARRANGE
        val randomId = UUID.randomUUID()
        every { tenantRepository.findById(randomId) } returns null

        // ACT + ASSERT
        val exception = assertThrows<IllegalArgumentException> {
            service.create(
                CreateGroupRequest(
                    tenantId = randomId.toString(),
                    name = "Equipe de Produção",
                    description = null
                )
            )
        }

        assertEquals("Tenant $randomId not found", exception.message)
        verify(exactly = 0) { saveGroup.save(any()) }
    }

    @Test
    fun `should persist group exactly once`() {
        // ARRANGE
        val tenant = buildTenant()
        every { tenantRepository.findById(tenant.id) } returns tenant
        every { saveGroup.save(any()) } answers { firstArg() }

        // ACT
        service.create(
            CreateGroupRequest(
                tenantId = tenant.id.toString(),
                name = "Equipe de Produção",
                description = null
            )
        )

        // ASSERT
        verify(exactly = 1) { saveGroup.save(any()) }
    }

    private fun buildTenant() = Tenant(
        id = UUID.randomUUID(),
        name = "Igreja Teste",
        apiKey = "api-key-teste",
        status = "ACTIVE"
    )
}