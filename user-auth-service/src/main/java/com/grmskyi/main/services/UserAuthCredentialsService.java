package com.grmskyi.main.services;

import com.grmskyi.main.pojos.AuthenticationRequest;
import com.grmskyi.main.pojos.AuthenticationResponse;
import com.grmskyi.main.pojos.RegistryRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserAuthCredentialsService {
    AuthenticationResponse register(RegistryRequest registryRequest);
    AuthenticationResponse login(AuthenticationRequest authenticationRequest);
    void logout(HttpServletRequest request, HttpServletResponse response);
}