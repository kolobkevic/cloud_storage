package ru.kolobkevic.cloud_storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kolobkevic.cloud_storage.models.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
