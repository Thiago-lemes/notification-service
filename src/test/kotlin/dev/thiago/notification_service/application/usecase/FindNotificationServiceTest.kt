package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.Notification
import dev.thiago.notification_service.domain.model.Tenant
import dev.thiago.notification_service.domain.port.output.FindNotificationPort
import dev.thiago.notification_service.domain.port.output.TenantRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class FindNotificationServiceTest {

    private val tenantRepository = mockk<TenantRepository>()
    private val findNotification = mockk<FindNotificationPort>()

    private val service = FindNotificationService(
        tenantRepository = tenantRepository,
        findNotification = findNotification
    )

    @Test
    fun `should return notification when api key and id are valid`() {
        // ARRANGE
        val tenant = buildTenant()
        val notification = buildNotification(tenantId = tenant.id)

        every { tenantRepository.findByApiKey(tenant.apiKey) } returns tenant
        every { findNotification.findById(notification.id) } returns notification

        // ACT
        val result = service.find(apiKey = tenant.apiKey, notificationId = notification.id)

        // ASSERT
        assertEquals(notification.id, result.id)
        assertEquals(notification.status, result.status)
        assertEquals(notification.payload, result.payload)
    }

    @Test
    fun `should throw exception when api key is invalid`() {
        // ARRANGE
        every { tenantRepository.findByApiKey(any()) } returns null

        // ACT + ASSERT
        val exception = assertThrows<IllegalArgumentException> {
            service.find(apiKey = "chave-invalida", notificationId = UUID.randomUUID())
        }

        assertEquals("Invalid API key", exception.message)
    }

    @Test
    fun `should throw exception when notification is not found`() {
        // ARRANGE
        val tenant = buildTenant()
        val randomId = UUID.randomUUID()

        every { tenantRepository.findByApiKey(tenant.apiKey) } returns tenant
        every { findNotification.findById(randomId) } returns null

        // ACT + ASSERT
        val exception = assertThrows<NoSuchElementException> {
            service.find(apiKey = tenant.apiKey, notificationId = randomId)
        }

        assertEquals("Notification $randomId not found", exception.message)
    }

    @Test
    fun `should throw exception when notification belongs to different tenant`() {
        // ARRANGE
        val tenant = buildTenant()
        val otherTenantId = UUID.randomUUID()
        val notification = buildNotification(tenantId = otherTenantId)

        every { tenantRepository.findByApiKey(tenant.apiKey) } returns tenant
        every { findNotification.findById(notification.id) } returns notification

        // ACT + ASSERT
        val exception = assertThrows<IllegalAccessException> {
            service.find(apiKey = tenant.apiKey, notificationId = notification.id)
        }

        assertEquals("Notification does not belong to this tenant", exception.message)
    }

    // helpers
    private fun buildTenant() = Tenant(
        id = UUID.randomUUID(),
        name = "Escola Teste",
        apiKey = "api-key-teste-001",
        status = "ACTIVE"
    )

    private fun buildNotification(tenantId: UUID) = Notification(
        id = UUID.randomUUID(),
        tenantId = tenantId,
        templateId = null,
        groupId = null,
        payload = mapOf("message" to "Amanhã não haverá aula")
    )
}