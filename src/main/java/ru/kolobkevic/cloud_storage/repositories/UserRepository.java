package ru.kolobkevic.cloud_storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kolobkevic.cloud_storage.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);

}
