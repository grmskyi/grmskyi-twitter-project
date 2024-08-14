package com.grmskyi.main.services.user_details;

import com.grmskyi.main.models.Role;
import com.grmskyi.main.models.UserCredentials;
import com.grmskyi.main.repositories.UserCredentialsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {
    @Mock
    private UserCredentialsRepository userCredentialsRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private UserCredentials userCredentials;

    @BeforeEach
    void setUp() {
        userCredentials = UserCredentials.builder()
                .email("john.doe@example.com")
                .password("encodedPassword123")
                .role(Role.USER)
                .build();
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWhenUserExists() {
        System.out.println("Starting test: loadUserByUsername_shouldReturnUserDetailsWhenUserExists");

        when(userCredentialsRepository.findByEmail("john.doe@example.com")).thenReturn(userCredentials);

        UserDetails result = userDetailsService.loadUserByUsername("john.doe@example.com");

        assertNotNull(result);
        assertEquals(userCredentials.getEmail(), result.getUsername());
        assertEquals(userCredentials.getPassword(), result.getPassword());

        verify(userCredentialsRepository).findByEmail("john.doe@example.com");

        System.out.println("User details loaded successfully for email: " + result.getUsername());
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionWhenUserNotFound() {
        System.out.println("Starting test: loadUserByUsername_shouldThrowExceptionWhenUserNotFound");

        when(userCredentialsRepository.findByEmail("john.doe@example.com")).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("john.doe@example.com")
        );

        assertEquals("User not found with email: john.doe@example.com", exception.getMessage());

        verify(userCredentialsRepository).findByEmail("john.doe@example.com");

        System.out.println("Expected exception caught when user not found: " + exception.getMessage());
    }
}