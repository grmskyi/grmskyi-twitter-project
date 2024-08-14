package com.example.userservice.configs;

import com.example.userservice.pojos.UserDTO;
import com.example.userservice.services.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitMQDataUserReceiverConfig {

    private final SubscriptionService subscriptionService;

    @Value("${rabbitmq.queues.listener}")
    private String queueForListener;

    @Bean
    public Queue queue() {
        return new Queue(queueForListener, false);
    }

    @RabbitListener(queues = "${rabbitmq.queues.listener}")
    public void listen(UserDTO userDTO) {
        log.info("Received message from queue {}: {}", queueForListener, userDTO);
        subscriptionService.getDataFromMQ(userDTO);
    }
}