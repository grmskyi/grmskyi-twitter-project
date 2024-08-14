package com.grmskyi.main.services.user_details;

import com.grmskyi.main.repositories.UserCredentialsRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserCredentialsRepository userCredentialsRepository;

    /**
     * Loads a user's details by their email address. This method is used by Spring Security
     * to authenticate users by their credentials.
     *
     * @param userEmail The email address of the user whose details are to be loaded.
     * @return A {@link UserDetails} object containing user credentials and authorities.
     * @throws UsernameNotFoundException If no user is found with the provided email address.
     */
    @Override
    @SneakyThrows
    public UserDetails loadUserByUsername(String userEmail) {
        UserDetails userDetails = userCredentialsRepository.findByEmail(userEmail);
        if (userDetails == null) {
            throw new UsernameNotFoundException("User not found with email: " + userEmail);
        }
        return userDetails;
    }
}