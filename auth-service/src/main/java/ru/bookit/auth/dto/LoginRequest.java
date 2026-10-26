package ru.bookit.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Укажите логин или email") String login,
        @NotBlank(message = "Укажите пароль") String password
) {
}
