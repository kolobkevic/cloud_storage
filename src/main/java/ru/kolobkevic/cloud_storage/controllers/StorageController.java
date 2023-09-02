package ru.kolobkevic.cloud_storage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import ru.kolobkevic.cloud_storage.services.StorageService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/storage")
public class StorageController {
    private final StorageService storageService;

    @GetMapping
    public String getFiles(@AuthenticationPrincipal User user,
                           @RequestParam(value = "path", required = false, defaultValue = "") String path,
                           Model model) {
        model.addAttribute("breadCrumbs", storageService.getBreadCrumb(path));
        model.addAttribute("files", storageService.getListOfObjects(user.getUsername(), path));

        return "cloud-storage";
    }

    @PostMapping
    public String uploadFile(@ModelAttribute("file") MultipartFile file,
                             @AuthenticationPrincipal User user,
                             @RequestParam(value = "path", required = false, defaultValue = "") String path) {
        storageService.uploadFile(user.getUsername(), file, path);

        return "redirect:/storage";
    }

    @PutMapping
    public String renameFile(@AuthenticationPrincipal User user,
                             @RequestParam(value = "oldPath") String oldPath,
                             @RequestParam(value = "newPath") String newPath) {
        storageService.renameObject(user.getUsername(), oldPath, newPath);
        return "redirect:/storage";
    }

    @DeleteMapping
    public String deleteFile(@AuthenticationPrincipal User user,
                             @RequestParam(value = "path") String path) {
        storageService.removeObject(user.getUsername(), path);
        return "redirect:/storage";
    }

    @GetMapping(produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<ByteArrayResource> downloadFile(@AuthenticationPrincipal User user,
                                                          @RequestParam(value = "filename") String filename) {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .body(storageService.downloadFile(user.getUsername(), filename));
    }
}
