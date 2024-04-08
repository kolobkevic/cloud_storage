package ru.kolobkevic.cloud_storage.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kolobkevic.cloud_storage.exceptions.StorageServerException;
import ru.kolobkevic.cloud_storage.exceptions.UserAlreadyExistsException;
import ru.kolobkevic.cloud_storage.models.User;
import ru.kolobkevic.cloud_storage.services.StorageService;
import ru.kolobkevic.cloud_storage.services.UserService;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final StorageService storageService;
    private static final String REGISTRATION_PAGE = "auth/registration";

    @GetMapping("/registration")
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new User());
        return REGISTRATION_PAGE;
    }

    @PostMapping("/registration")
    public String register(@ModelAttribute("user") @Valid User user, BindingResult bindingResult)
            throws StorageServerException {
        if (bindingResult.hasErrors()) {
            return REGISTRATION_PAGE;
        }

        try {
            userService.create(user);
            storageService.createUserFolder(user.getEmail());
        } catch (UserAlreadyExistsException e) {
            log.debug("Sign up failed for user " + user.getEmail());
            bindingResult.rejectValue("email", "user already exists",
                    "User with this email already exists");
            return REGISTRATION_PAGE;
        }
        return "redirect:auth/login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }
}
