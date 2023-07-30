package ru.kolobkevic.cloud_storage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kolobkevic.cloud_storage.repositories.RoleRepository;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
}
