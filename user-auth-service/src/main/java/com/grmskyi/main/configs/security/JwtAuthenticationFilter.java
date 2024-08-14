package com.grmskyi.main.configs.security;

import com.grmskyi.main.services.jwt.JwtService;
import com.grmskyi.main.services.user_details.UserDetailsServiceImpl;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Filters each HTTP request for JWT authentication, processes token if present,
     * and handles any exceptions that arise during the authentication process.
     *
     * @param request     The request to filter.
     * @param response    The response associated with the request.
     * @param filterChain The filter chain for invoking the next filter or the resource at the end of the chain.
     * @throws IOException In case of input/output errors.
     */
    @Override
    @SneakyThrows
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) {

        try {
            processTokenAuthentication(request);
        } catch (Exception ex) {
            handleAuthenticationError(response, ex);
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Processes the JWT token from the 'Authorization' header of the request.
     * If the header is present and correctly formatted, it forwards the token for authentication.
     *
     * @param request The HTTP request with the JWT token.
     */
    private void processTokenAuthentication(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            authenticateRequest(jwt, request);
        }
    }

    /**
     * Attempts to authenticate the request using the provided JWT token.
     * If the user's email is extracted successfully and no prior authentication exists,
     * it validates the token and sets the security context.
     *
     * @param jwt     The JWT token used for authenticating the request.
     * @param request The HTTP request being processed.
     */
    private void authenticateRequest(String jwt, HttpServletRequest request) {
        String userEmail = jwtService.extractUsername(jwt);
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                setSecurityContext(userDetails, request);
            } else {
                log.debug("JWT token is not valid");
            }
        } else {
            log.debug("User email not found or request already authenticated");
        }
    }

    /**
     * Sets the security context for the current session, authenticating the user
     * with the provided user details and the application's security environment.
     *
     * @param userDetails The details of the user to authenticate.
     * @param request     The HTTP request for which the user is authenticated.
     */
    private void setSecurityContext(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        log.debug("Security context set for user: {}", userDetails.getUsername());
    }

    /**
     * Handles errors that occur during the authentication process by logging the exception
     * and setting the HTTP response status to 401 Unauthorized.
     *
     * @param response The HTTP response to modify.
     * @param ex       The exception that was caught during authentication.
     */
    private void handleAuthenticationError(HttpServletResponse response, Exception ex) {
        log.error("Authentication error: ", ex);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}