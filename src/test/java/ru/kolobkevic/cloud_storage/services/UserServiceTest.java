package ru.kolobkevic.cloud_storage.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kolobkevic.cloud_storage.entities.Role;
import ru.kolobkevic.cloud_storage.entities.User;
import ru.kolobkevic.cloud_storage.entities.enums.RoleType;
import ru.kolobkevic.cloud_storage.repositories.UserRepository;

import java.util.Set;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService userService;

    @Mock
    private static PasswordEncoder passwordEncoder;

    private static User createUser(){
        User user = new User();
        user.setFirstName("First name");
        user.setLastName("Last name");
        user.setEmail("email@mail.ru");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(Set.of(createRole()));
        return user;
    }

    private static Role createRole(){
        Role role = new Role();
        role.setName("User");
        role.setDescription("Simple user");
        role.setRoleType(RoleType.USER);
        return role;
    }

    @Test
    void create() {
        User user = createUser();

        when(repository.save(user)).thenReturn(new User());
        userService.create(user);

        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any());
    }

}