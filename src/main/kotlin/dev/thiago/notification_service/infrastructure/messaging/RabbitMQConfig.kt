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
            .withArgument("x-dead-letter-exchange", "$EXCHANGE.dlx")
            .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
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