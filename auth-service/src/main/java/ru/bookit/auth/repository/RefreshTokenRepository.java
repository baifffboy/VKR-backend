package ru.bookit.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bookit.auth.entity.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
