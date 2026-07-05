package dev.thiago.notification_service.infrastructure.persistence

import dev.thiago.notification_service.domain.model.Notification
import dev.thiago.notification_service.infrastructure.persistence.notification.NotificationJpaRepository
import dev.thiago.notification_service.infrastructure.persistence.notification.NotificationRepositoryAdapter
import dev.thiago.notification_service.infrastructure.persistence.tenant.TenantJpaEntity
import dev.thiago.notification_service.infrastructure.persistence.tenant.TenantJpaRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import java.util.UUID

class NotificationRepositoryAdapterTest(
    @Autowired private val jpaRepository: NotificationJpaRepository,
    @Autowired private val tenantJpaRepository: TenantJpaRepository
) : RepositoryIntegrationTest() {

    private val adapter = NotificationRepositoryAdapter(jpaRepository)

    @Test
    fun `should save and find notification by id`() {
        // ARRANGE
        val tenantId = createTenant()
        val notification = Notification(
            tenantId = tenantId,
            templateId = null,
            groupId = null,
            payload = mapOf("message" to "Amanhã não haverá aula")
        )

        // ACT
        adapter.save(notification)
        val result = adapter.findById(notification.id)

        // ASSERT
        assertNotNull(result)
        assertEquals(notification.id, result!!.id)
        assertEquals("PENDING", result.status)
        assertEquals(tenantId, result.tenantId)
    }

    @Test
    fun `should return null when notification does not exist`() {
        // ACT
        val result = adapter.findById(UUID.randomUUID())

        // ASSERT
        assertNull(result)
    }

    @Test
    fun `should find notifications by tenant id with pagination`() {
        // ARRANGE
        val tenantId = createTenant()
        val otherTenantId = createTenant()

        repeat(3) {
            adapter.save(Notification(
                tenantId = tenantId,
                templateId = null,
                groupId = null,
                payload = mapOf("message" to "Aviso $it")
            ))
        }

        // notificação de outro tenant — não deve aparecer
        adapter.save(Notification(
            tenantId = otherTenantId,
            templateId = null,
            groupId = null,
            payload = mapOf("message" to "Outro tenant")
        ))

        // ACT
        val pageable = PageRequest.of(0, 10)
        val result = adapter.findByTenantId(tenantId, pageable)

        // ASSERT
        assertEquals(3, result.totalElements)
        assertTrue(result.content.all { it.tenantId == tenantId })
    }

    @Test
    fun `should paginate correctly`() {
        // ARRANGE
        val tenantId = createTenant()

        repeat(5) {
            adapter.save(Notification(
                tenantId = tenantId,
                templateId = null,
                groupId = null,
                payload = mapOf("message" to "Aviso $it")
            ))
        }

        // ACT
        val page0 = adapter.findByTenantId(tenantId, PageRequest.of(0, 2))
        val page1 = adapter.findByTenantId(tenantId, PageRequest.of(1, 2))

        // ASSERT
        assertEquals(5, page0.totalElements)
        assertEquals(3, page0.totalPages)
        assertEquals(2, page0.content.size)
        assertEquals(2, page1.content.size)
    }

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
}