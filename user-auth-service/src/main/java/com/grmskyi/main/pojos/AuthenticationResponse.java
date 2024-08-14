package com.grmskyi.main.pojos;

import lombok.*;

/**
 * A stub class for local use representing the authentication response.
 *
 * <p>This class is primarily used to encapsulate the JWT access token generated
 * during the authentication process. It includes methods to retrieve the access token
 * for further processing or validation, such as extracting user information from the JWT.</p>
 *
 * <p>Note: This is intended for local development and testing purposes.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private String accessToken;
}