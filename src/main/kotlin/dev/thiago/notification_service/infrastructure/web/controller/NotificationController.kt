package dev.thiago.notification_service.infrastructure.web.controller

import dev.thiago.notification_service.domain.port.input.FindNotificationUseCase
import dev.thiago.notification_service.domain.port.input.SendNotificationRequest
import dev.thiago.notification_service.domain.port.input.SendNotificationUseCase
import dev.thiago.notification_service.infrastructure.web.dto.request.NotificationRequest
import dev.thiago.notification_service.infrastructure.web.dto.response.NotificationResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

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
        val notification = sendNotification.send(
            apiKey = apiKey,
            request = SendNotificationRequest(
                templateId = request.templateId,
                groupId = request.groupId,
                payload = request.payload
            )
        )
        return ResponseEntity.accepted()
            .body(mapOf("id" to notification.id, "status" to notification.status))
    }

    @GetMapping("/{id}")
    fun findById(
        @RequestHeader("X-API-Key") apiKey: String,
        @PathVariable id: UUID
    ): ResponseEntity<Any> {
        val notification = findNotification.find(apiKey = apiKey, notificationId = id)
        return ResponseEntity.ok(NotificationResponse.from(notification))
    }
}

