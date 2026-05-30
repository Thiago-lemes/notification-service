package dev.thiago.notification_service.application.usecase

import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.domain.model.RecipientGroup
import dev.thiago.notification_service.domain.port.output.AddGroupMemberPort
import dev.thiago.notification_service.domain.port.output.FindGroupPort
import dev.thiago.notification_service.domain.port.output.FindRecipientsByTenantPort
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class AddGroupMemberServiceTest {

    private val findGroup = mockk<FindGroupPort>()
    private val findRecipients = mockk<FindRecipientsByTenantPort>()
    private val addGroupMember = mockk<AddGroupMemberPort>()

    private val service = AddGroupMemberService(
        findGroup = findGroup,
        findRecipients = findRecipients,
        addGroupMember = addGroupMember
    )

    @Test
    fun `should add recipient to group when both exist in same tenant`() {
        // ARRANGE
        val group = buildGroup()
        val recipient = buildRecipient(tenantId = group.tenantId)

        every { findGroup.findById(group.id) } returns group
        every { findRecipients.findByTenantId(group.tenantId) } returns listOf(recipient)
        justRun { addGroupMember.add(group.id, recipient.id) }

        // ACT
        service.add(groupId = group.id, recipientId = recipient.id)

        // ASSERT
        verify(exactly = 1) { addGroupMember.add(group.id, recipient.id) }
    }

    @Test
    fun `should throw exception when group not found`() {
        // ARRANGE
        val randomGroupId = UUID.randomUUID()
        every { findGroup.findById(randomGroupId) } returns null

        // ACT + ASSERT
        val exception = assertThrows<IllegalArgumentException> {
            service.add(groupId = randomGroupId, recipientId = UUID.randomUUID())
        }

        assertEquals("Group $randomGroupId not found", exception.message)
        verify(exactly = 0) { addGroupMember.add(any(), any()) }
    }

    @Test
    fun `should throw exception when recipient does not belong to tenant`() {
        // ARRANGE
        val group = buildGroup()
        val recipientFromAnotherTenant = buildRecipient(tenantId = UUID.randomUUID())
        val unknownRecipientId = UUID.randomUUID()

        every { findGroup.findById(group.id) } returns group
        every { findRecipients.findByTenantId(group.tenantId) } returns listOf(recipientFromAnotherTenant)

        // ACT + ASSERT
        val exception = assertThrows<IllegalArgumentException> {
            service.add(groupId = group.id, recipientId = unknownRecipientId)
        }

        assertEquals("Recipient $unknownRecipientId not found in tenant", exception.message)
        verify(exactly = 0) { addGroupMember.add(any(), any()) }
    }

    @Test
    fun `should add member exactly once`() {
        // ARRANGE
        val group = buildGroup()
        val recipient = buildRecipient(tenantId = group.tenantId)

        every { findGroup.findById(group.id) } returns group
        every { findRecipients.findByTenantId(group.tenantId) } returns listOf(recipient)
        justRun { addGroupMember.add(group.id, recipient.id) }

        // ACT
        service.add(groupId = group.id, recipientId = recipient.id)

        // ASSERT
        verify(exactly = 1) { addGroupMember.add(any(), any()) }
    }

    @Test
    fun `should add correct recipient when multiple recipients exist in tenant`() {
        // ARRANGE
        val group = buildGroup()
        val recipient1 = buildRecipient(tenantId = group.tenantId)
        val recipient2 = buildRecipient(tenantId = group.tenantId)
        val recipient3 = buildRecipient(tenantId = group.tenantId)

        every { findGroup.findById(group.id) } returns group
        every { findRecipients.findByTenantId(group.tenantId) } returns listOf(recipient1, recipient2, recipient3)
        justRun { addGroupMember.add(group.id, recipient2.id) }

        // ACT
        service.add(groupId = group.id, recipientId = recipient2.id)

        // ASSERT
        verify(exactly = 1) { addGroupMember.add(group.id, recipient2.id) }
        verify(exactly = 0) { addGroupMember.add(group.id, recipient1.id) }
        verify(exactly = 0) { addGroupMember.add(group.id, recipient3.id) }
    }

    private fun buildGroup() = RecipientGroup(
        id = UUID.randomUUID(),
        tenantId = UUID.randomUUID(),
        name = "Equipe de Produção",
        description = "Voluntários da cozinha"
    )

    private fun buildRecipient(tenantId: UUID) = Recipient(
        id = UUID.randomUUID(),
        tenantId = tenantId,
        name = "João da Silva",
        email = "joao@cantina.com",
        phone = null,
        channelPreferences = listOf("EMAIL")
    )
}