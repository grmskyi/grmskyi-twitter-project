package com.grmskyi.main.services;

import com.grmskyi.main.models.Role;
import com.grmskyi.main.models.UserCredentials;
import com.grmskyi.main.pojos.AuthenticationRequest;
import com.grmskyi.main.pojos.AuthenticationResponse;
import com.grmskyi.main.pojos.RegistryRequest;
import com.grmskyi.main.repositories.UserCredentialsRepository;
import com.grmskyi.main.services.jwt.JwtService;
import com.grmskyi.main.services.messaging.RabbitMqMessagingServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthCredentialsServiceImpl implements UserAuthCredentialsService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserCredentialsRepository userCredentialsRepository;
    private final RabbitMqMessagingServiceImpl rabbitMqMessagingService;

    /**
     * Registers a new user with the provided credentials. This method checks for email uniqueness,
     * saves the user credentials in the repository, sends the credentials to an external service,
     * and generates a JWT token for the new user.
     *
     * @param registryRequest The registration request containing user credentials.
     * @return An {@link AuthenticationResponse} containing a JWT access token.
     * @throws ResponseStatusException If the email is already in use.
     */
    @Override
    public AuthenticationResponse register(RegistryRequest registryRequest) {
        if (emailExists(registryRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
        }

        var userCredentials = UserCredentials.builder()
                .firstName(registryRequest.getFirstName())
                .lastName(registryRequest.getLastName())
                .email(registryRequest.getEmail())
                .password(passwordEncoder.encode(registryRequest.getPassword()))
                .role(Role.USER)
                .build();

        log.info("User registered successfully with email: {}", registryRequest.getEmail());
        userCredentialsRepository.save(userCredentials);

        rabbitMqMessagingService.sendUserCredentials(userCredentials);

        return AuthenticationResponse.builder()
                .accessToken(jwtService.generateToken(userCredentials))
                .build();
    }

    /**
     * Authenticates a user with the provided credentials. This method uses the
     * {@link AuthenticationManager} to authenticate the user and generates a JWT token if successful.
     *
     * @param authenticationRequest The authentication request containing user credentials.
     * @return An {@link AuthenticationResponse} containing a JWT access token.
     */
    @Override
    public AuthenticationResponse login(AuthenticationRequest authenticationRequest) {
        log.info("Attempting to login user with email: {}", authenticationRequest.getEmail());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getEmail(),
                authenticationRequest.getPassword()
        ));

        var userCredentials = userCredentialsRepository.findByEmail(authenticationRequest.getEmail());

        return AuthenticationResponse.builder()
                .accessToken(jwtService.generateToken(userCredentials))
                .build();
    }

    /**
     * Logs out the current user by clearing the security context, invalidating the session,
     * and removing the JWT cookie.
     * This method performs the necessary steps to log out a user securely:
     * <li>Clears the {@link SecurityContextHolder}, removing any authentication information.</li>
     * <li>Invalidates the current HTTP session, if one exists, to prevent session fixation attacks.</li>
     * <li>Deletes the JWT cookie by setting its value to {@code null} and its max age to 0, effectively removing it from the client's browser.</li>
     * After these steps, a logout message is logged.
     *
     * @param request  the {@link HttpServletRequest} from which the session and cookies are retrieved.
     * @param response the {@link HttpServletResponse} to which the modified JWT cookie is added.
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        Cookie jwtCookie = new Cookie("JWT", null);

        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        log.info("User logged out and session invalidated.");
    }

    /**
     * Checks if an email is already registered in the system.
     *
     * @param email The email address to check.
     * @return {@code true} if the email exists; {@code false} otherwise.
     */
    private boolean emailExists(String email) {
        return userCredentialsRepository.findByEmail(email) != null;
    }
}