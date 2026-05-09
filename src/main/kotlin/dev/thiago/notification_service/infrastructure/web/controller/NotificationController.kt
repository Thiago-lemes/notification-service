package dev.thiago.notification_service.infrastructure.web.controller

import dev.thiago.notification_service.domain.port.input.FindNotificationUseCase
import dev.thiago.notification_service.domain.port.input.SendNotificationRequest
import dev.thiago.notification_service.domain.port.input.SendNotificationUseCase
import dev.thiago.notification_service.infrastructure.web.dto.NotificationRequest
import dev.thiago.notification_service.infrastructure.web.dto.NotificationResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val sendNotification: SendNotificationUseCase,
    private val findNotification: FindNotificationUseCase
) {

    @PostMapping
    fun send(
        @RequestHeader("X-API-Key") apiKey: String,
        @RequestBody request: NotificationRequest
    ): ResponseEntity<Any> {
        return try {
            val notification = sendNotification.send(
                apiKey = apiKey,
                request = SendNotificationRequest(
                    templateId = request.templateId,
                    groupId = request.groupId,
                    payload = request.payload
                )
            )
            ResponseEntity.accepted().body(mapOf("id" to notification.id, "status" to notification.status))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/{id}")
    fun findById(
        @RequestHeader("X-API-Key") apiKey: String,
        @PathVariable id: UUID
    ): ResponseEntity<Any> {
        return try {
            val notification = findNotification.find(apiKey = apiKey, notificationId = id)
            ResponseEntity.ok(NotificationResponse.from(notification))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to e.message))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        }
    }
}

