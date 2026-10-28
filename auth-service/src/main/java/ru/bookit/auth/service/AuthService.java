package ru.bookit.auth.service;

import ru.bookit.auth.dto.AuthResponse;
import ru.bookit.auth.dto.LoginRequest;
import ru.bookit.auth.dto.RegisterRequest;
import ru.bookit.auth.dto.UserResponse;

import java.util.UUID;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);

    UserResponse me(UUID userId);
}