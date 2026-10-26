package ru.bookit.auth.service;

import ru.bookit.auth.dto.AuthResponse;
import ru.bookit.auth.dto.LoginRequest;
import ru.bookit.auth.dto.RegisterRequest;
import ru.bookit.auth.dto.UserResponse;

import java.util.UUID;

public interface AuthService {

    /**
     * Регистрирует нового пользователя. Бросает ConflictException, если email или логин заняты.
     */
    UserResponse register(RegisterRequest request);

    /**
     * Вход по логину или email. Бросает UnauthorizedException при неверных данных.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Меняет refresh-токен на новую пару токенов (старый refresh при этом отзывается).
     */
    AuthResponse refresh(String refreshToken);

    /**
     * Отзывает refresh-токен.
     */
    void logout(String refreshToken);

    /**
     * Возвращает данные пользователя по id из access-токена.
     */
    UserResponse me(UUID userId);
}