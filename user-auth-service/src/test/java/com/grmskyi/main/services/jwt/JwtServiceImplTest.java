package com.grmskyi.main.services.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    private String token;
    private SecretKey secretKey;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("john.doe@example.com");

        String secret = "mySuperSecretKeyThatIsLongEnoughToBeSecure123!";
        byte[] keyBytes = Decoders.BASE64.decode(secret);

        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", secret);
        secretKey = Keys.hmacShaKeyFor(keyBytes);

        token = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(secretKey)
                .compact();
    }

    @Test
    void extractUsername_shouldReturnUsernameFromToken() {
        System.out.println("Starting test: extractUsername_shouldReturnUsernameFromToken");

        String extractedUsername = jwtService.extractUsername(token);

        assertNotNull(extractedUsername);
        assertEquals("john.doe@example.com", extractedUsername);

        System.out.println("Username extracted successfully from token: " + extractedUsername);
    }

    @Test
    void generateToken_shouldGenerateValidJwtToken() {
        System.out.println("Starting test: generateToken_shouldGenerateValidJwtToken");

        String generatedToken = jwtService.generateToken(userDetails);

        assertNotNull(generatedToken);
        assertNotEquals("", generatedToken);

        String extractedUsername = jwtService.extractUsername(generatedToken);

        assertEquals("john.doe@example.com", extractedUsername);

        System.out.println("JWT token generated and verified successfully with username: " + extractedUsername);
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        System.out.println("Starting test: isTokenValid_shouldReturnTrueForValidToken");

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);

        System.out.println("Token validated successfully and is valid.");
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {
        System.out.println("Starting test: isTokenValid_shouldReturnFalseForInvalidToken");

        when(userDetails.getUsername()).thenReturn("wrong.email@example.com");

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertFalse(isValid);

        System.out.println("Token validation failed as expected due to incorrect username.");
    }

    @Test
    void isTokenExpired_shouldReturnTrueForExpiredToken() {
        System.out.println("Starting test: isTokenExpired_shouldReturnTrueForExpiredToken");

        String expiredToken = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60))
                .expiration(new Date(System.currentTimeMillis() - 1000 * 60))
                .signWith(secretKey)
                .compact();

        boolean isExpired = Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(jwtService, "isTokenExpired", expiredToken));

        assertTrue(isExpired);

        System.out.println("Expired token correctly identified as expired.");
    }

    @Test
    void extractClaims_shouldReturnClaimsFromToken() {
        System.out.println("Starting test: extractClaims_shouldReturnClaimsFromToken");

        Claims claims = jwtService.extractClaims(token, Function.identity());

        assertNotNull(claims);
        assertEquals("john.doe@example.com", claims.getSubject());

        System.out.println("Claims extracted successfully from token.");
    }

    @Test
    void generateTokenWithClaims_shouldGenerateTokenWithExtraClaims() {
        System.out.println("Starting test: generateTokenWithClaims_shouldGenerateTokenWithExtraClaims");

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");

        String generatedToken = jwtService.generateToken(extraClaims, userDetails);

        assertNotNull(generatedToken);
        assertNotEquals("", generatedToken);

        Claims claims = jwtService.extractClaims(generatedToken, Function.identity());

        assertEquals("john.doe@example.com", claims.getSubject());
        assertEquals("ADMIN", claims.get("role"));

        System.out.println("JWT token generated with extra claims and verified successfully.");
    }
}