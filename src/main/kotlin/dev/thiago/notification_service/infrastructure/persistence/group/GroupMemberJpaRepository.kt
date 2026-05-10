package dev.thiago.notification_service.infrastructure.persistence.group

import org.springframework.data.jpa.repository.JpaRepository

interface GroupMemberJpaRepository : JpaRepository<GroupMemberJpaEntity, GroupMemberId>