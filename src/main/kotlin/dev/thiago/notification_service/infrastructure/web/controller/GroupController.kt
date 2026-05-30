package dev.thiago.notification_service.infrastructure.web.controller

import dev.thiago.notification_service.domain.port.input.AddGroupMemberUseCase
import dev.thiago.notification_service.domain.port.input.CreateGroupRequest
import dev.thiago.notification_service.domain.port.input.CreateGroupUseCase
import dev.thiago.notification_service.infrastructure.web.dto.response.GroupResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import dev.thiago.notification_service.infrastructure.web.dto.request.CreateGroupRequest as CreateGroupRequestDto

@RestController
@RequestMapping("/groups")
class GroupController(
    private val createGroup: CreateGroupUseCase,
    private val addGroupMember: AddGroupMemberUseCase
) {

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateGroupRequestDto
    ): ResponseEntity<Any> {
        return try {
            val group = createGroup.create(
                CreateGroupRequest(
                    tenantId = request.tenantId,
                    name = request.name,
                    description = request.description
                )
            )
            ResponseEntity.status(HttpStatus.CREATED).body(GroupResponse.from(group))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/{groupId}/members")
    fun addMember(
        @PathVariable groupId: UUID,
        @RequestParam recipientId: UUID
    ): ResponseEntity<Any> {
        return try {
            addGroupMember.add(groupId, recipientId)
            ResponseEntity.ok(mapOf("message" to "Recipient added to group"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}