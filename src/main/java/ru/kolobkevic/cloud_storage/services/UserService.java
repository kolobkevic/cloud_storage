package ru.kolobkevic.cloud_storage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.kolobkevic.cloud_storage.exceptions.UserAlreadyExistsException;
import ru.kolobkevic.cloud_storage.models.User;
import ru.kolobkevic.cloud_storage.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException(String.format("User with email '%s' not found", username)));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getRoles().stream().map(
                        role -> new SimpleGrantedAuthority(role.getRoleType().name())).toList()
        );
    }

    public User create(User user) throws UserAlreadyExistsException {
        encodeUserPassword(user);
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            var message = e.getMessage().split("'");
            if (user.getEmail().equals(message[1])) {
                throw new UserAlreadyExistsException("Email is not unique");
            } else throw e;
        }
    }

    private void encodeUserPassword(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
    }
}
