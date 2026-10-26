package ru.bookit.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Укажите email")
        @Email(message = "Некорректный email")
        @Size(max = 255, message = "Email слишком длинный")
        String email,

        @NotBlank(message = "Укажите логин")
        @Size(min = 3, max = 50, message = "Логин: от 3 до 50 символов")
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Логин: только латиница, цифры и _ . -")
        String login,

        @NotBlank(message = "Укажите пароль")
        @Size(min = 8, max = 72, message = "Пароль: от 8 до 72 символов")
        String password
) {
}
