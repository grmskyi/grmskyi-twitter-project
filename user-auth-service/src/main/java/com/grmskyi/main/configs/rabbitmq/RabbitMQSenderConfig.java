package com.grmskyi.main.configs.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQSenderConfig {

    @Value("${rabbitmq.queues.value}")
    private String queueName;

    @Value("${rabbitmq.exchanges.value}")
    private String exchangeName;

    /**
     * Defines a {@link Queue} bean with the queue name specified in the application properties.
     * The queue is non-durable.
     *
     * @return the configured {@link Queue} instance.
     */
    @Bean
    public Queue queue() {
        return new Queue(queueName, false);
    }

    /**
     * Defines a {@link TopicExchange} bean with the exchange name specified in the application properties.
     * The exchange is non-durable and not auto-deleted.
     *
     * @return the configured {@link Exchange} instance.
     */
    @Bean
    Exchange exchange() {
        return new TopicExchange(exchangeName, false, false);
    }

    /**
     * Creates a {@link Binding} bean that binds the specified queue to the specified exchange with a routing key.
     * The binding uses a routing key 'first.key' with no additional arguments.
     *
     * @param queue    The queue to bind.
     * @param exchange The exchange to bind the queue to.
     * @return the configured {@link Binding} instance.
     */
    @Bean
    Binding binding(Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("first.key").noargs();
    }
}