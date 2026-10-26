package ru.bookit.auth.dto;

import ru.bookit.auth.entity.Role;

import java.util.UUID;

public record UserResponse(UUID id, String email, String login, Role role) {
}
