package dev.thiago.notification_service.infrastructure.persistence

import dev.thiago.notification_service.domain.model.Recipient
import dev.thiago.notification_service.infrastructure.persistence.group.GroupMemberJpaEntity
import dev.thiago.notification_service.infrastructure.persistence.group.GroupMemberId
import dev.thiago.notification_service.infrastructure.persistence.group.GroupMemberJpaRepository
import dev.thiago.notification_service.infrastructure.persistence.group.RecipientGroupJpaEntity
import dev.thiago.notification_service.infrastructure.persistence.group.RecipientGroupJpaRepository
import dev.thiago.notification_service.infrastructure.persistence.recipient.RecipientJpaRepository
import dev.thiago.notification_service.infrastructure.persistence.recipient.RecipientRepositoryAdapter
import dev.thiago.notification_service.infrastructure.persistence.tenant.TenantJpaEntity
import dev.thiago.notification_service.infrastructure.persistence.tenant.TenantJpaRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class RecipientRepositoryAdapterTest(
    @Autowired private val jpaRepository: RecipientJpaRepository,
    @Autowired private val tenantJpaRepository: TenantJpaRepository,
    @Autowired private val groupJpaRepository: RecipientGroupJpaRepository,
    @Autowired private val groupMemberJpaRepository: GroupMemberJpaRepository
) : RepositoryIntegrationTest() {

    private val adapter = RecipientRepositoryAdapter(jpaRepository)

    @Test
    fun `should save and find recipients by tenant id`() {
        // ARRANGE
        val tenantId = createTenant()
        val otherTenantId = createTenant()

        adapter.save(buildRecipient(tenantId, "joao@escola.com"))
        adapter.save(buildRecipient(tenantId, "maria@escola.com"))
        adapter.save(buildRecipient(otherTenantId, "outro@escola.com"))

        // ACT
        val result = adapter.findByTenantId(tenantId)

        // ASSERT
        assertEquals(2, result.size)
        assertTrue(result.all { it.tenantId == tenantId })
    }

    @Test
    fun `should find recipients by group id`() {
        // ARRANGE
        val tenantId = createTenant()
        val groupId = createGroup(tenantId)

        val recipient1 = adapter.save(buildRecipient(tenantId, "joao@escola.com"))
        val recipient2 = adapter.save(buildRecipient(tenantId, "maria@escola.com"))
        val recipientNotInGroup = adapter.save(buildRecipient(tenantId, "outro@escola.com"))

        groupMemberJpaRepository.save(GroupMemberJpaEntity(GroupMemberId(groupId, recipient1.id)))
        groupMemberJpaRepository.save(GroupMemberJpaEntity(GroupMemberId(groupId, recipient2.id)))

        // ACT
        val result = adapter.findByGroupId(groupId)

        // ASSERT
        assertEquals(2, result.size)
        assertTrue(result.none { it.id == recipientNotInGroup.id })
    }

    @Test
    fun `should return empty list when group has no members`() {
        // ARRANGE
        val tenantId = createTenant()
        val groupId = createGroup(tenantId)

        // ACT
        val result = adapter.findByGroupId(groupId)

        // ASSERT
        assertTrue(result.isEmpty())
    }

    private fun buildRecipient(tenantId: UUID, email: String) = Recipient(
        id = UUID.randomUUID(),
        tenantId = tenantId,
        name = "Recipient Teste",
        email = email,
        phone = null,
        webhookUrl = null,
        channelPreferences = listOf("EMAIL")
    )

    private fun createTenant(): UUID {
        val tenant = TenantJpaEntity(
            id = UUID.randomUUID(),
            name = "Tenant Teste",
            apiKey = UUID.randomUUID().toString(),
            status = "ACTIVE"
        )
        tenantJpaRepository.save(tenant)
        return tenant.id
    }

    private fun createGroup(tenantId: UUID): UUID {
        val group = RecipientGroupJpaEntity(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            name = "Grupo Teste",
            description = null
        )
        groupJpaRepository.save(group)
        return group.id
    }
}