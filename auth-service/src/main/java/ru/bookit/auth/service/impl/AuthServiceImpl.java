package ru.bookit.auth.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookit.auth.dto.AuthResponse;
import ru.bookit.auth.dto.LoginRequest;
import ru.bookit.auth.dto.RegisterRequest;
import ru.bookit.auth.dto.UserResponse;
import ru.bookit.auth.entity.RefreshToken;
import ru.bookit.auth.entity.User;
import ru.bookit.auth.exception.ConflictException;
import ru.bookit.auth.exception.UnauthorizedException;
import ru.bookit.auth.repository.RefreshTokenRepository;
import ru.bookit.auth.repository.UserRepository;
import ru.bookit.auth.security.JwtService;
import ru.bookit.auth.service.AuthService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    UserRepository users;
    RefreshTokenRepository refreshTokens;
    PasswordEncoder encoder;
    JwtService jwtService;

    @Value("${app.jwt.refresh-ttl-days}")
    long refreshTtlDays;

    SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String login = request.login().trim().toLowerCase();

        if (users.existsByEmail(email)) {
            throw new ConflictException("Email уже зарегистрирован");
        }
        if (users.existsByLogin(login)) {
            throw new ConflictException("Логин уже занят");
        }

        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setPasswordHash(encoder.encode(request.password()));
        try {
            return toResponse(users.saveAndFlush(user));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email или логин уже заняты");
        }
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = request.login().trim().toLowerCase();
        User user = users.findByLoginOrEmail(identifier, identifier)
                .filter(u -> encoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Неверный логин или пароль"));
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String rawToken) {
        RefreshToken token = refreshTokens.findByTokenHash(sha256(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Недействительный refresh-токен"));
        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Недействительный refresh-токен");
        }
        token.setRevoked(true);
        return issueTokens(token.getUser());
    }

    @Override
    @Transactional
    public void logout(String rawToken) {
        refreshTokens.findByTokenHash(sha256(rawToken)).ifPresent(t -> t.setRevoked(true));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse me(UUID userId) {
        return users.findById(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new UnauthorizedException("Пользователь не найден"));
    }

    private AuthResponse issueTokens(User user) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String rawRefresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(sha256(rawRefresh));
        token.setExpiresAt(Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS));
        refreshTokens.save(token);

        return new AuthResponse(
                jwtService.generateAccessToken(user),
                rawRefresh,
                jwtService.accessTtlSeconds(),
                toResponse(user));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getLogin(), user.getRole());
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}