package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.Tenant
import dev.thiago.notification_service.domain.port.input.CreateRecipientRequest
import dev.thiago.notification_service.domain.port.output.SaveRecipientPort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class CreateRecipientServiceTest {

    private val tenantRepository = mockk<TenantRepository>()
    private val saveRecipient = mockk<SaveRecipientPort>()

    private val service = CreateRecipientService(
        tenantRepository = tenantRepository,
        saveRecipient = saveRecipient
    )

    @Test
    fun `should create recipient when request is valid`() {
        // ARRANGE
        val tenant = buildTenant()
        every { tenantRepository.findById(tenant.id) } returns tenant
        every { saveRecipient.save(any()) } answers { firstArg() }

        // ACT
        val result = service.create(buildRequest(tenantId = tenant.id.toString()))

        // ASSERT
        assertNotNull(result.id)
        assertEquals("João da Silva", result.name)
        assertEquals("joao@cantina.com", result.email)
        assertEquals(listOf("EMAIL"), result.channelPreferences)
        verify(exactly = 1) { saveRecipient.save(any()) }
    }

    @Test
    fun `should throw exception when tenant not found`() {
        // ARRANGE
        val randomId = UUID.randomUUID()
        every { tenantRepository.findById(randomId) } returns null

        // ACT + ASSERT
        val exception = assertThrows<IllegalArgumentException> {
            service.create(buildRequest(tenantId = randomId.toString()))
        }

        assertEquals("Tenant $randomId not found", exception.message)
        verify(exactly = 0) { saveRecipient.save(any()) }
    }

    @Test
    fun `should throw exception when recipient has no email and no phone`() {
        // ARRANGE
        val tenant = buildTenant()
        every { tenantRepository.findById(tenant.id) } returns tenant

        // ACT + ASSERT
        val exception = assertThrows<IllegalArgumentException> {
            service.create(
                buildRequest(
                    tenantId = tenant.id.toString(), email = null, phone = null, webhookUrl = null
                )
            )
        }

        assertEquals("Recipient must have at least one contact — email or phone", exception.message)
        verify(exactly = 0) { saveRecipient.save(any()) }
    }

    @Test
    fun `should create recipient with phone only`() {
        // ARRANGE
        val tenant = buildTenant()
        every { tenantRepository.findById(tenant.id) } returns tenant
        every { saveRecipient.save(any()) } answers { firstArg() }

        // ACT
        val result = service.create(
            buildRequest(tenantId = tenant.id.toString(), email = null, phone = "+5541999999999")
        )

        // ASSERT
        assertEquals(null, result.email)
        assertEquals("+5541999999999", result.phone)
    }

    @Test
    fun `should create recipient with multiple channel preferences`() {
        // ARRANGE
        val tenant = buildTenant()
        every { tenantRepository.findById(tenant.id) } returns tenant
        every { saveRecipient.save(any()) } answers { firstArg() }

        // ACT
        val result = service.create(
            buildRequest(
                tenantId = tenant.id.toString(),
                phone = "+5541999999999",
                channelPreferences = listOf("EMAIL", "WHATSAPP")
            )
        )

        // ASSERT
        assertEquals(listOf("EMAIL", "WHATSAPP"), result.channelPreferences)
    }

    private fun buildTenant() = Tenant(
        id = UUID.randomUUID(),
        name = "Cantina Teste",
        apiKey = "api-key-teste",
        status = "ACTIVE"
    )

    private fun buildRequest(
        tenantId: String = UUID.randomUUID().toString(),
        email: String? = "joao@cantina.com",
        phone: String? = null,
        webhookUrl: String? = null,
        channelPreferences: List<String> = listOf("EMAIL")
    ) = CreateRecipientRequest(
        tenantId = tenantId,
        name = "João da Silva",
        email = email,
        phone = phone,
        channelPreferences = channelPreferences,
        webhookUrl = webhookUrl
    )
}