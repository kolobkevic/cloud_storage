package ru.kolobkevic.cloud_storage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kolobkevic.cloud_storage.models.User;
import ru.kolobkevic.cloud_storage.services.StorageService;
import ru.kolobkevic.cloud_storage.services.UserService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class RegistrationController {
    private final UserService userService;
    private final StorageService storageService;

    @GetMapping("/registration")
    public String showRegistrationPage(){
        return "auth/registration";
    }

    @PostMapping("/registration")
    public String register(@ModelAttribute("user") User user){
        try {
            userService.create(user);
            storageService.createUserFolder(user.getEmail());
        }
        catch (Exception e) {
            return "auth/registration";
        }
        return "redirect:auth/login";
    }

    @GetMapping("/login")
    public String showLoginPage(){
        return "auth/login";
    }
}
