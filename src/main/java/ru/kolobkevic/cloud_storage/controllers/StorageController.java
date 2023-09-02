package ru.kolobkevic.cloud_storage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kolobkevic.cloud_storage.models.StorageObject;
import ru.kolobkevic.cloud_storage.services.StorageService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/storage")
public class StorageController {
    private final StorageService storageService;

    @GetMapping
    public List<StorageObject> getFiles(@AuthenticationPrincipal User user,
                                        @RequestParam(value = "path", required = false, defaultValue = "") String path,
                                        Model model) {
        return storageService.getListOfObjects(user.getUsername(), path);
    }
}
