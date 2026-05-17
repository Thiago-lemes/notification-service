package dev.thiago.notification_service.infrastructure.persistence.recipient

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface RecipientJpaRepository : JpaRepository<RecipientJpaEntity, UUID> {
    fun findByTenantId(tenantId: UUID): List<RecipientJpaEntity>

    @Query("""
        SELECT r FROM RecipientJpaEntity r
        INNER JOIN GroupMemberJpaEntity gm ON gm.id.recipientId = r.id
        WHERE gm.id.groupId = :groupId
    """)
    fun findByGroupId(@Param("groupId") groupId: UUID): List<RecipientJpaEntity>
}