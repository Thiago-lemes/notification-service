package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.Notification
import dev.thiago.notification_service.domain.model.Tenant
import dev.thiago.notification_service.domain.port.input.SendNotificationRequest
import dev.thiago.notification_service.domain.port.output.NotificationPublisherPort
import dev.thiago.notification_service.domain.port.output.SaveNotificationPort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class SendNotificationServiceTest {

    // mocks das dependências — implementações falsas das interfaces
    private val tenantRepository = mockk<TenantRepository>()
    private val saveNotification = mockk<SaveNotificationPort>()
    private val publishNotification = mockk<NotificationPublisherPort>()

    // o que estamos testando — recebe os mocks no construtor
    private val service = SendNotificationService(
        tenantRepository = tenantRepository,
        saveNotification = saveNotification,
        publishNotification = publishNotification
    )

    @Test
    fun `should create and persist notification when api key is valid`() {
        // ARRANGE — prepara o cenário
        val tenant = buildTenant()
        val request = buildRequest()

        every { tenantRepository.findByApiKey(tenant.apiKey) } returns tenant
        every { saveNotification.save(any()) } answers { firstArg() }
        justRun { publishNotification.publish(any()) }

        // ACT — executa o que está sendo testado
        val result = service.send(apiKey = tenant.apiKey, request = request)

        // ASSERT — verifica o resultado
        assertNotNull(result.id)
        assertEquals("PENDING", result.status)
        assertEquals(tenant.id, result.tenantId)

        // verifica que salvou e publicou exatamente uma vez
        verify(exactly = 1) { saveNotification.save(any()) }
        verify(exactly = 1) { publishNotification.publish(any()) }
    }

    @Test
    fun `should throw exception when api key is invalid`() {
        // ARRANGE
        every { tenantRepository.findByApiKey(any()) } returns null

        val request = buildRequest()

        // ACT + ASSERT
        val exception = assertThrows<IllegalArgumentException> {
            service.send(apiKey = "chave-invalida", request = request)
        }

        assertEquals("Invalid API key", exception.message)

        // verifica que nunca salvou nem publicou
        verify(exactly = 0) { saveNotification.save(any()) }
        verify(exactly = 0) { publishNotification.publish(any()) }
    }

    @Test
    fun `should publish notification after saving`() {
        // ARRANGE
        val tenant = buildTenant()
        val request = buildRequest()
        val savedNotification = Notification(
            tenantId = tenant.id,
            templateId = null,
            groupId = null,
            payload = request.payload
        )

        every { tenantRepository.findByApiKey(tenant.apiKey) } returns tenant
        every { saveNotification.save(any()) } returns savedNotification
        justRun { publishNotification.publish(any()) }

        // ACT
        service.send(apiKey = tenant.apiKey, request = request)

        // ASSERT — verifica a ordem: salva primeiro, publica depois
        verify(exactly = 1) { saveNotification.save(any()) }
        verify(exactly = 1) { publishNotification.publish(savedNotification) }
    }

    @Test
    fun `should map payload correctly from request to notification`() {
        // ARRANGE
        val tenant = buildTenant()
        val payload = mapOf("message" to "Amanhã não haverá aula", "subject" to "Aviso")
        val request = SendNotificationRequest(
            templateId = null,
            groupId = null,
            payload = payload
        )

        every { tenantRepository.findByApiKey(tenant.apiKey) } returns tenant
        every { saveNotification.save(any()) } answers { firstArg() }
        justRun { publishNotification.publish(any()) }

        // ACT
        val result = service.send(apiKey = tenant.apiKey, request = request)

        // ASSERT
        assertEquals(payload, result.payload)
    }

    // helpers para construir objetos de teste
    private fun buildTenant() = Tenant(
        id = UUID.randomUUID(),
        name = "Escola Teste",
        apiKey = "api-key-teste-001",
        status = "ACTIVE"
    )

    private fun buildRequest() = SendNotificationRequest(
        templateId = null,
        groupId = null,
        payload = mapOf("message" to "Amanhã não haverá aula")
    )
}