package com.example.userservice.configs;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configures and returns a {@link RabbitTemplate} with custom settings for the application's messaging needs.
     * This method sets up a {@link RabbitTemplate} with a specific {@link ConnectionFactory} and a message converter
     * that serializes and deserializes messages to and from JSON format using {@link Jackson2JsonMessageConverter}.
     *
     * @param connectionFactory    the factory that creates connections to the RabbitMQ server, enabling the template
     *                             to connect to the queue manager.
     * @param jsonMessageConverter the converter used to convert messages to and from JSON format, facilitating the
     *                             handling of message conversion using Jackson 2.
     * @return a fully configured {@link RabbitTemplate} ready for use in sending and receiving messages.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}