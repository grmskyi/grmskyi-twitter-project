package com.example.userservice;

import com.example.userservice.configs.RabbitMQDataUserReceiverConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class UserServiceApplicationTests {

	@MockBean
	RabbitMQDataUserReceiverConfig rabbitMQDataUserReceiverConfig;

	@Test
	void contextLoads() {
	}
}
