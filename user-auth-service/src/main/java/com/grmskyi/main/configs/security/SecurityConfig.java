package com.grmskyi.main.configs.security;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configures the security filter chain for HTTP requests.
     * It sets up CSRF protection, session management, and authentication handling
     * to secure the web application.
     *
     * @param http The HttpSecurity configuration passed by Spring Security for customization.
     * @return A fully configured SecurityFilterChain.
     * @throws Exception if an error occurs during the configuration.
     */
    @Bean
    @SneakyThrows
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request.requestMatchers(
                                "/api/v1/auth/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(getAuthenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Provides an authentication provider that uses a data access object (DAO) for user authentication.
     * This bean sets up the service to load user details and the password encoder for user password comparison.
     *
     * @return An initialized DaoAuthenticationProvider.
     */
    @Bean
    public AuthenticationProvider getAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    /**
     * Defines a bean for password encoding.
     * This method returns a BCryptPasswordEncoder, which uses the BCrypt strong hashing function.
     *
     * @return A BCryptPasswordEncoder to be used across the application.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the default AuthenticationManager as a Spring bean.
     * This is used to process authentication requests.
     *
     * @param configuration Authentication configuration responsible for creating an authentication manager.
     * @return The default AuthenticationManager.
     * @throws Exception if there's an issue creating the AuthenticationManager.
     */
    @Bean
    @SneakyThrows
    public AuthenticationManager getAuthenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    /**
     * Configures web security to ignore specific paths from Spring Security filtering.
     * <p>
     * This method creates a {@link WebSecurityCustomizer} bean that tells Spring Security
     * to bypass security filtering for the specified paths. In this case, it ignores requests
     * to the Swagger UI and API documentation endpoints, allowing them to be accessed without
     * authentication or authorization.
     * </p>
     *
     * @return a {@link WebSecurityCustomizer} that configures Spring Security to ignore the specified paths.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/swagger-ui/**", "/api-docs/**"
        );
    }
}