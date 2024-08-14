package com.grmskyi.main.services.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${encryption.key.value}")
    private String SECRET_KEY;

    /**
     * Extracts the username from a JWT token.
     *
     * @param token The JWT token from which to extract the username.
     * @return The username extracted from the token.
     */
    @Override
    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    /**
     * Extracts claims from a JWT token and applies a specified function to process the claims.
     *
     * @param token          The JWT token from which to extract claims.
     * @param claimsResolver A function to process the extracted claims.
     * @param <T>            The type of the result returned by the claimsResolver function.
     * @return The result obtained by applying the claimsResolver function to the extracted claims.
     */
    @Override
    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a JWT token for a user with no additional claims.
     *
     * @param userDetails The user details for whom to generate the token.
     * @return A JWT token string.
     */
    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Validates a JWT token by checking if the username extracted from the token matches
     * the username from the user details and if the token is not expired.
     *
     * @param token       The JWT token to validate.
     * @param userDetails The user details against which to validate the token.
     * @return {@code true} if the token is valid; {@code false} otherwise.
     */
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Generates a JWT token with additional claims for a specified user.
     *
     * @param extraClaims Additional claims to include in the token.
     * @param userDetails The user details for whom to generate the token.
     * @return A JWT token string.
     */
    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Checks if a JWT token is expired by comparing its expiration date to the current date.
     * <p>
     * This method attempts to extract the expiration date from the provided JWT token and
     * compares it with the current date. If the token has expired, it returns {@code true}.
     * If the token is already expired and an {@link ExpiredJwtException} is thrown during
     * the extraction of the expiration date, this method catches the exception and also
     * returns {@code true}, confirming the token's expiration.
     * </p>
     *
     * @param token The JWT token to check.
     * @return {@code true} if the token is expired; {@code false} otherwise.
     */
    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token The JWT token from which to extract the expiration date.
     * @return The expiration date extracted from the token.
     */
    private Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    /**
     * Retrieves the claims contained within a JWT token.
     *
     * @param token The JWT token from which to retrieve claims.
     * @return The claims extracted from the token.
     */
    private Claims getClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Generates a signing key from the secret key configured in the application properties.
     * The key is used to sign and verify JWT tokens.
     *
     * @return A {@link SecretKey} used for signing JWT tokens.
     */
    private SecretKey getSignInKey() {
        var keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}