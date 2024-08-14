package com.grmskyi.main.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grmskyi.main.pojos.AuthenticationRequest;
import com.grmskyi.main.pojos.AuthenticationResponse;
import com.grmskyi.main.pojos.RegistryRequest;
import com.grmskyi.main.repositories.UserCredentialsRepository;
import com.grmskyi.main.services.messaging.RabbitMqMessagingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    @MockBean
    private RabbitMqMessagingServiceImpl rabbitMqMessagingService;

    @BeforeEach
    void setup() {
        userCredentialsRepository.deleteAll();
        Mockito.doNothing().when(rabbitMqMessagingService).sendUserCredentials(Mockito.any());
    }

    @Test
    void testRegisterUser() throws Exception {
        System.out.println("Starting integration test: testRegisterUser");

        RegistryRequest registryRequest = new RegistryRequest();
        registryRequest.setFirstName("John");
        registryRequest.setLastName("Doe");
        registryRequest.setEmail("john.doe@example.com");
        registryRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registryRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(jsonResponse, AuthenticationResponse.class);

        assertNotNull(response.getAccessToken());
        System.out.println("User registered successfully with token: " + response.getAccessToken());
    }

    @Test
    void testRegisterUser_shouldReturnConflictWhenEmailExists() throws Exception {
        System.out.println("Starting integration test: testRegisterUser_shouldReturnConflictWhenEmailExists");

        RegistryRequest registryRequest = new RegistryRequest();
        registryRequest.setFirstName("John");
        registryRequest.setLastName("Doe");
        registryRequest.setEmail("john.doe@example.com");
        registryRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registryRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registryRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();

        System.out.println("Registration attempt with existing email returned conflict as expected.");
    }

    @Test
    void testLoginUser() throws Exception {
        System.out.println("Starting integration test: testLoginUser");

        RegistryRequest registryRequest = new RegistryRequest();
        registryRequest.setFirstName("John");
        registryRequest.setLastName("Doe");
        registryRequest.setEmail("john.doe@example.com");
        registryRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registryRequest)))
                .andExpect(status().isOk());


        AuthenticationRequest loginRequest = new AuthenticationRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(jsonResponse, AuthenticationResponse.class);

        assertNotNull(response.getAccessToken());
        System.out.println("User logged in successfully with token: " + response.getAccessToken());
    }

    @Test
    @WithMockUser
    void testLogoutUser() throws Exception {
        System.out.println("Starting integration test: testLogoutUser");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        System.out.println("User logged out successfully.");
    }
}