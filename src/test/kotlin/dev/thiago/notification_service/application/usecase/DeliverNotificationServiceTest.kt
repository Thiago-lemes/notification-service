package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.Notification
import dev.thiago.notification_service.domain.model.NotificationEvent
import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.domain.port.output.*
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class DeliverNotificationServiceTest {

    private val findNotification = mockk<FindNotificationPort>()
    private val findRecipients = mockk<FindRecipientsByTenantPort>()
    private val saveDelivery = mockk<SaveDeliveryPort>()
    private val emailChannel = mockk<NotificationChannelPort>()
    private val whatsappChannel = mockk<NotificationChannelPort>()

    private val findRecipientsByGroup = mockk<FindRecipientsByGroupPort>()

    private val service = DeliverNotificationService(
        findNotification = findNotification,
        findRecipients = findRecipients,
        findRecipientsByGroup = findRecipientsByGroup,
        saveDelivery = saveDelivery,
        channels = listOf(emailChannel, whatsappChannel)
    )

    @Test
    fun `should deliver via email when recipient prefers email`() {
        // ARRANGE
        val notification = buildNotification()
        val recipient = buildRecipient(channelPreferences = listOf("EMAIL"))
        val event = buildEvent(notification)

        every { findNotification.findById(notification.id) } returns notification
        every { findRecipients.findByTenantId(notification.tenantId) } returns listOf(recipient)
        every { saveDelivery.save(any()) } answers { firstArg() }
        every { emailChannel.supports("EMAIL") } returns true
        every { whatsappChannel.supports("EMAIL") } returns false
        justRun { emailChannel.deliver(recipient, notification.payload) }

        // ACT
        service.deliver(event)

        // ASSERT — email entregue, whatsapp nunca chamado
        verify(exactly = 1) { emailChannel.deliver(recipient, notification.payload) }
        verify(exactly = 0) { whatsappChannel.deliver(any(), any()) }
    }

    @Test
    fun `should deliver via whatsapp when recipient prefers whatsapp`() {
        // ARRANGE
        val notification = buildNotification()
        val recipient = buildRecipient(channelPreferences = listOf("WHATSAPP"))
        val event = buildEvent(notification)

        every { findNotification.findById(notification.id) } returns notification
        every { findRecipients.findByTenantId(notification.tenantId) } returns listOf(recipient)
        every { saveDelivery.save(any()) } answers { firstArg() }
        every { emailChannel.supports("WHATSAPP") } returns false
        every { whatsappChannel.supports("WHATSAPP") } returns true
        justRun { whatsappChannel.deliver(recipient, notification.payload) }

        // ACT
        service.deliver(event)

        // ASSERT
        verify(exactly = 1) { whatsappChannel.deliver(recipient, notification.payload) }
        verify(exactly = 0) { emailChannel.deliver(any(), any()) }
    }

    @Test
    fun `should deliver via all preferred channels when recipient prefers both`() {
        // ARRANGE
        val notification = buildNotification()
        val recipient = buildRecipient(channelPreferences = listOf("EMAIL", "WHATSAPP"))
        val event = buildEvent(notification)

        every { findNotification.findById(notification.id) } returns notification
        every { findRecipients.findByTenantId(notification.tenantId) } returns listOf(recipient)
        every { saveDelivery.save(any()) } answers { firstArg() }
        every { emailChannel.supports("EMAIL") } returns true
        every { emailChannel.supports("WHATSAPP") } returns false
        every { whatsappChannel.supports("EMAIL") } returns false
        every { whatsappChannel.supports("WHATSAPP") } returns true
        justRun { emailChannel.deliver(any(), any()) }
        justRun { whatsappChannel.deliver(any(), any()) }

        // ACT
        service.deliver(event)

        // ASSERT — ambos os canais acionados
        verify(exactly = 1) { emailChannel.deliver(recipient, notification.payload) }
        verify(exactly = 1) { whatsappChannel.deliver(recipient, notification.payload) }
    }

    @Test
    fun `should save delivery as DELIVERED when channel succeeds`() {
        // ARRANGE
        val notification = buildNotification()
        val recipient = buildRecipient(channelPreferences = listOf("EMAIL"))
        val event = buildEvent(notification)

        every { findNotification.findById(notification.id) } returns notification
        every { findRecipients.findByTenantId(notification.tenantId) } returns listOf(recipient)
        every { saveDelivery.save(any()) } answers { firstArg() }
        every { emailChannel.supports("EMAIL") } returns true
        justRun { emailChannel.deliver(any(), any()) }

        // ACT
        service.deliver(event)

        // ASSERT — salva PENDING e depois DELIVERED
        verify(exactly = 1) {
            saveDelivery.save(match { it.status == "PENDING" })
        }
        verify(exactly = 1) {
            saveDelivery.save(match { it.status == "DELIVERED" })
        }
    }

    @Test
    fun `should save delivery as FAILED when channel throws exception`() {
        // ARRANGE
        val notification = buildNotification()
        val recipient = buildRecipient(channelPreferences = listOf("EMAIL"))
        val event = buildEvent(notification)

        every { findNotification.findById(notification.id) } returns notification
        every { findRecipients.findByTenantId(notification.tenantId) } returns listOf(recipient)
        every { saveDelivery.save(any()) } answers { firstArg() }
        every { emailChannel.supports("EMAIL") } returns true
        every { emailChannel.deliver(any(), any()) } throws RuntimeException("SMTP unavailable")

        // ACT
        service.deliver(event)

        // ASSERT — salva FAILED com mensagem de erro
        verify(exactly = 1) {
            saveDelivery.save(match {
                it.status == "FAILED" && it.errorMessage == "SMTP unavailable"
            })
        }
    }

    @Test
    fun `should throw exception when notification is not found`() {
        // ARRANGE
        val event = buildEvent(buildNotification())
        every { findNotification.findById(any()) } returns null

        // ACT + ASSERT
        assertThrows<IllegalArgumentException> {
            service.deliver(event)
        }
    }

    @Test
    fun `should deliver to all recipients in tenant`() {
        // ARRANGE
        val notification = buildNotification()
        val recipient1 = buildRecipient(channelPreferences = listOf("EMAIL"))
        val recipient2 = buildRecipient(channelPreferences = listOf("EMAIL"))
        val event = buildEvent(notification)

        every { findNotification.findById(notification.id) } returns notification
        every { findRecipients.findByTenantId(notification.tenantId) } returns listOf(recipient1, recipient2)
        every { saveDelivery.save(any()) } answers { firstArg() }
        every { emailChannel.supports("EMAIL") } returns true
        justRun { emailChannel.deliver(any(), any()) }

        // ACT
        service.deliver(event)

        // ASSERT — email entregue para os 2 destinatários
        verify(exactly = 2) { emailChannel.deliver(any(), notification.payload) }
    }

    @Test
    fun `should deliver to group recipients when groupId is present`() {
        // ARRANGE
        val notification = buildNotification(groupId = UUID.randomUUID())
        val recipient = buildRecipient(channelPreferences = listOf("EMAIL"))
        val event = buildEvent(notification)

        every { findNotification.findById(notification.id) } returns notification
        every { findRecipientsByGroup.findByGroupId(notification.groupId!!) } returns listOf(recipient)
        every { saveDelivery.save(any()) } answers { firstArg() }
        every { emailChannel.supports("EMAIL") } returns true
        justRun { emailChannel.deliver(any(), any()) }

        // ACT
        service.deliver(event)

        // ASSERT — busca por grupo, não por tenant
        verify(exactly = 1) { findRecipientsByGroup.findByGroupId(notification.groupId!!) }
        verify(exactly = 0) { findRecipients.findByTenantId(any()) }
        verify(exactly = 1) { emailChannel.deliver(recipient, notification.payload) }
    }

    @Test
    fun `should deliver to all tenant recipients when groupId is null`() {
        // ARRANGE
        val notification = buildNotification(groupId = null)
        val recipient = buildRecipient(channelPreferences = listOf("EMAIL"))
        val event = buildEvent(notification)

        every { findNotification.findById(notification.id) } returns notification
        every { findRecipients.findByTenantId(notification.tenantId) } returns listOf(recipient)
        every { saveDelivery.save(any()) } answers { firstArg() }
        every { emailChannel.supports("EMAIL") } returns true
        justRun { emailChannel.deliver(any(), any()) }

        // ACT
        service.deliver(event)

        // ASSERT — busca por tenant, não por grupo
        verify(exactly = 1) { findRecipients.findByTenantId(notification.tenantId) }
        verify(exactly = 0) { findRecipientsByGroup.findByGroupId(any()) }
    }

    // helpers
    private fun buildNotification(groupId: UUID? = null) = Notification(
        id = UUID.randomUUID(),
        tenantId = UUID.randomUUID(),
        templateId = null,
        groupId = groupId,
        payload = mapOf("message" to "Amanhã não haverá aula", "subject" to "Aviso")
    )

    private fun buildRecipient(channelPreferences: List<String>) = Recipient(
        id = UUID.randomUUID(),
        tenantId = UUID.randomUUID(),
        name = "Maria Silva",
        email = "maria@escola.com",
        phone = null,
        channelPreferences = channelPreferences
    )

    private fun buildEvent(notification: Notification) = NotificationEvent(
        notificationId = notification.id,
        tenantId = notification.tenantId,
        payload = notification.payload
    )
}