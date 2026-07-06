package dev.thiago.notification_service.infrastructure.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

@Component
class NotificationMetrics(private val registry: MeterRegistry) {

    fun incrementSent() {
        Counter.builder("notifications.sent")
            .description("Total de notificações enviadas")
            .register(registry)
            .increment()
    }

    fun incrementDelivered(channel: String) {
        Counter.builder("notifications.delivered")
            .tag("channel", channel)
            .description("Total de notificações entregues por canal")
            .register(registry)
            .increment()
    }

    fun incrementFailed(channel: String) {
        Counter.builder("notifications.failed")
            .tag("channel", channel)
            .description("Total de falhas por canal")
            .register(registry)
            .increment()
    }

    fun incrementRetry() {
        Counter.builder("notifications.retry")
            .description("Total de retries")
            .register(registry)
            .increment()
    }

    fun incrementDlq() {
        Counter.builder("notifications.dlq")
            .description("Total de mensagens enviadas para DLQ")
            .register(registry)
            .increment()
    }

    fun recordDeliveryTime(channel: String, durationMs: Long) {
        Timer.builder("notifications.delivery.duration")
            .tag("channel", channel)
            .description("Tempo de entrega por canal em ms")
            .register(registry)
            .record(java.time.Duration.ofMillis(durationMs))
    }
}