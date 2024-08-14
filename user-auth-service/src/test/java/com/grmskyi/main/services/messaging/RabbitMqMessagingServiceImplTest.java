package com.grmskyi.main.services.messaging;

import com.grmskyi.main.models.Role;
import com.grmskyi.main.models.UserCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMqMessagingServiceImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMqMessagingServiceImpl rabbitMqMessagingService;

    private UserCredentials userCredentials;

    @BeforeEach
    void setUp() {
        userCredentials = UserCredentials.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword123")
                .role(Role.USER)
                .build();

        ReflectionTestUtils.setField(rabbitMqMessagingService, "routingKey", "test.routing.key");
        ReflectionTestUtils.setField(rabbitMqMessagingService, "exchangeName", "test.exchange");
    }

    @Test
    void sendUserCredentials_shouldSendUserCredentialsToRabbitMQ() {
        System.out.println("Starting test: sendUserCredentials_shouldSendUserCredentialsToRabbitMQ");

        rabbitMqMessagingService.sendUserCredentials(userCredentials);

        verify(rabbitTemplate).convertAndSend(
                "test.exchange",
                "test.routing.key",
                userCredentials
        );

        System.out.println("User credentials sent successfully to exchange 'test.exchange' with routing key 'test.routing.key'.");
    }
}