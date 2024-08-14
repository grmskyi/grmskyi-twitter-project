package com.grmskyi.main.services.messaging;

import com.grmskyi.main.models.UserCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqMessagingServiceImpl {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.routing.key.value}")
    private String routingKey;

    @Value("${rabbitmq.exchanges.value}")
    private String exchangeName;

    /**
     * Sends a {@link UserCredentials} object to a configured RabbitMQ exchange and routing key.
     * This method logs the attempt and details of the message being sent.
     *
     * @param userCredentials The user credentials object to be sent as a message.
     */
    public void sendUserCredentials(UserCredentials userCredentials) {
        log.info("Attempting to send user credentials to exchange '{}' with routing key '{}': {}", exchangeName, routingKey, userCredentials);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, userCredentials);
    }
}