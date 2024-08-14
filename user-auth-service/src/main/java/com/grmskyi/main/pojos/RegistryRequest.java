package com.grmskyi.main.pojos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryRequest {
    @NotEmpty(message = "First name cannot be empty")
    @Size(max = 255, message = "First name be longer than 255 characters")
    private String firstName;

    @NotEmpty(message = "Last name cannot be empty")
    @Size(max = 255, message = "Last name be longer than 255 characters")
    private String lastName;

    @Indexed(unique = true)
    @Email(regexp = ".+[@].+[\\.].+")
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 8, max = 50, message = "The password must be between 8 and 50 characters")
    private String password;
}