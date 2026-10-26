package ru.bookit.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bookit.auth.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    boolean existsByLogin(String login);

    Optional<User> findByLoginOrEmail(String login, String email);
}
