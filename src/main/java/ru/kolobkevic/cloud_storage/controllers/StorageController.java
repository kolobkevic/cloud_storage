package ru.kolobkevic.cloud_storage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kolobkevic.cloud_storage.models.StorageObject;
import ru.kolobkevic.cloud_storage.services.StorageService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/storage")
public class StorageController {
    private final StorageService storageService;

    @GetMapping
    public List<StorageObject> getFiles(String str) {
        return storageService.getListOfObjects(str);
    }
}
