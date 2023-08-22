package ru.kolobkevic.cloud_storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kolobkevic.cloud_storage.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
