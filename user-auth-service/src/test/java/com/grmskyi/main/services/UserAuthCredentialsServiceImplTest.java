package com.grmskyi.main.services;

import com.grmskyi.main.models.Role;
import com.grmskyi.main.models.UserCredentials;
import com.grmskyi.main.pojos.AuthenticationRequest;
import com.grmskyi.main.pojos.AuthenticationResponse;
import com.grmskyi.main.pojos.RegistryRequest;
import com.grmskyi.main.repositories.UserCredentialsRepository;
import com.grmskyi.main.services.jwt.JwtService;
import com.grmskyi.main.services.messaging.RabbitMqMessagingServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthCredentialsServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserCredentialsRepository userCredentialsRepository;

    @Mock
    private RabbitMqMessagingServiceImpl rabbitMqMessagingService;

    @InjectMocks
    private UserAuthCredentialsServiceImpl userAuthCredentialsService;

    private RegistryRequest registryRequest;
    private UserCredentials userCredentials;
    private AuthenticationRequest authenticationRequest;

    @BeforeEach
    void setUp() {
        registryRequest = RegistryRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .build();

        authenticationRequest = AuthenticationRequest.builder()
                .email("john.doe@example.com")
                .password("password123")
                .build();

        userCredentials = UserCredentials.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword123")
                .role(Role.USER)
                .build();
    }

    @Test
    void register_shouldRegisterUserSuccessfully() {
        System.out.println("Starting test: register_shouldRegisterUserSuccessfully");

        when(userCredentialsRepository.findByEmail("john.doe@example.com")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(jwtService.generateToken(userCredentials)).thenReturn("jwtToken");

        AuthenticationResponse result = userAuthCredentialsService.register(registryRequest);

        assertNotNull(result);
        assertEquals("jwtToken", result.getAccessToken());

        verify(userCredentialsRepository).save(userCredentials);
        verify(rabbitMqMessagingService).sendUserCredentials(userCredentials);

        System.out.println("User registered successfully with token: " + result.getAccessToken());
    }

    @Test
    void register_shouldThrowExceptionWhenEmailAlreadyExists() {
        System.out.println("Starting test: register_shouldThrowExceptionWhenEmailAlreadyExists");

        when(userCredentialsRepository.findByEmail("john.doe@example.com")).thenReturn(userCredentials);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> userAuthCredentialsService.register(registryRequest)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Email already in use", exception.getReason());

        verify(userCredentialsRepository, never()).save(any());
        verify(rabbitMqMessagingService, never()).sendUserCredentials(any());

        System.out.println("Expected exception when email already exists caught: " + exception.getReason());
    }

    @Test
    void login_shouldAuthenticateUserSuccessfully() {
        System.out.println("Starting test: login_shouldAuthenticateUserSuccessfully");

        when(userCredentialsRepository.findByEmail("john.doe@example.com")).thenReturn(userCredentials);
        when(jwtService.generateToken(userCredentials)).thenReturn("jwtToken");

        AuthenticationResponse result = userAuthCredentialsService.login(authenticationRequest);

        assertNotNull(result);
        assertEquals("jwtToken", result.getAccessToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        System.out.println("User authenticated successfully with token: " + result.getAccessToken());
    }

    @Test
    void logout_shouldClearSecurityContextAndInvalidateSession() {
        System.out.println("Starting test: logout_shouldClearSecurityContextAndInvalidateSession");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession(false)).thenReturn(session);

        userAuthCredentialsService.logout(request, response);

        verify(session).invalidate();
        verify(response).addCookie(argThat(cookie -> "JWT" .equals(cookie.getName()) && cookie.getMaxAge() == 0));

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        System.out.println("User logged out, session invalidated, and JWT cookie removed.");
    }
}