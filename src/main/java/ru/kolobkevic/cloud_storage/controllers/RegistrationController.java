package ru.kolobkevic.cloud_storage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.kolobkevic.cloud_storage.entities.User;
import ru.kolobkevic.cloud_storage.services.UserService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class RegistrationController {
    private final UserService userService;

    @GetMapping("/registration")
    public String registrationPage(){
        return "auth/registration";
    }

    @PostMapping("/registration")
    public String register(@ModelAttribute("user") User user){
        try {
            userService.create(user);
        }
        catch (Exception e) {
            return "auth/registration";
        }
        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginPage(){
        return "auth/login";
    }
}
