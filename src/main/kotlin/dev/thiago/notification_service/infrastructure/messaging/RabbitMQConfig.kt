package dev.thiago.notification_service.infrastructure.messaging

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    companion object {
        const val EXCHANGE = "notifications.exchange"
        const val QUEUE = "notifications.queue"
        const val DLQ = "notifications.dlq"
        const val ROUTING_KEY = "notification.send"
        const val DLQ_ROUTING_KEY = "notification.dead"
        const val MAX_RETRY_COUNT = 4
    }

    @Bean
    fun notificationsExchange(): DirectExchange =
        DirectExchange(EXCHANGE)

    @Bean
    fun deadLetterExchange(): DirectExchange =
        DirectExchange("$EXCHANGE.dlx")

    @Bean
    fun notificationsQueue(): Queue =
        QueueBuilder.durable(QUEUE)
            .withArgument("x-dead-letter-exchange", EXCHANGE)
            .withArgument("x-dead-letter-routing-key", ROUTING_KEY)
            .build()

    @Bean
    fun retry2sQueue(): Queue = buildRetryQueue("notifications.retry.2s", 2000)

    @Bean
    fun retry4sQueue(): Queue = buildRetryQueue("notifications.retry.4s", 4000)

    @Bean
    fun retry8sQueue(): Queue = buildRetryQueue("notifications.retry.8s", 8000)

    @Bean
    fun retry16sQueue(): Queue = buildRetryQueue("notifications.retry.16s", 16000)

    @Bean
    fun retry32sQueue(): Queue = buildRetryQueue("notifications.retry.32s", 32000)

    private fun buildRetryQueue(name: String, ttlMs: Int): Queue =
        QueueBuilder.durable(name)
            .withArgument("x-dead-letter-exchange", EXCHANGE)
            .withArgument("x-dead-letter-routing-key", ROUTING_KEY)
            .withArgument("x-message-ttl", ttlMs)
            .build()

    @Bean
    fun deadLetterQueue(): Queue =
        QueueBuilder.durable(DLQ).build()

    @Bean
    fun binding(): Binding =
        BindingBuilder.bind(notificationsQueue())
            .to(notificationsExchange())
            .with(ROUTING_KEY)

    @Bean
    fun retry2sBinding(): Binding =
        BindingBuilder
            .bind(retry2sQueue())
            .to(notificationsExchange())
            .with("notification.retry.2s")

    @Bean
    fun retry4sBinding(): Binding =
        BindingBuilder.bind(retry4sQueue()).to(notificationsExchange()).with("notification.retry.4s")

    @Bean
    fun retry8sBinding(): Binding =
        BindingBuilder.bind(retry8sQueue()).to(notificationsExchange()).with("notification.retry.8s")

    @Bean
    fun retry16sBinding(): Binding =
        BindingBuilder.bind(retry16sQueue()).to(notificationsExchange()).with("notification.retry.16s")

    @Bean
    fun retry32sBinding(): Binding =
        BindingBuilder.bind(retry32sQueue()).to(notificationsExchange()).with("notification.retry.32s")

    @Bean
    fun dlqBinding(): Binding =
        BindingBuilder.bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with(DLQ_ROUTING_KEY)

    @Bean
    fun messageConverter(): JacksonJsonMessageConverter =
        JacksonJsonMessageConverter()

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: JacksonJsonMessageConverter
    ): RabbitTemplate = RabbitTemplate(connectionFactory).apply {
        this.messageConverter = messageConverter
    }
}