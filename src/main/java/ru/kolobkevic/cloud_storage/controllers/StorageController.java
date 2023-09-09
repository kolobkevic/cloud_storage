package ru.kolobkevic.cloud_storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.kolobkevic.cloud_storage.dtos.FileDto;
import ru.kolobkevic.cloud_storage.dtos.FileRenameRequestDto;
import ru.kolobkevic.cloud_storage.dtos.FileRequestDto;
import ru.kolobkevic.cloud_storage.services.StorageService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/storage")
@Slf4j
public class StorageController {
    private final StorageService storageService;

    @GetMapping
    public String getFiles(@AuthenticationPrincipal User user,
                           @RequestParam(value = "path", required = false, defaultValue = "") String path,
                           Model model) {
        log.info("Path: " + path);
        model.addAttribute("breadCrumbs", storageService.getBreadCrumb(path));
        model.addAttribute("files", storageService.getListOfObjects(user.getUsername(), path));
        model.addAttribute("username", user.getUsername());

        return "cloud-storage";
    }

    @PostMapping
    public String uploadFile(@ModelAttribute("fileDto") FileDto fileDto) {
        storageService.uploadFile(fileDto.getUsername(), fileDto.getFile(), fileDto.getFile().getName());
        return "redirect:/storage";
    }

    @PutMapping
    public String renameFile(@ModelAttribute("fileRenameRequest") FileRenameRequestDto fileRenameRequestDto) {
        var username = fileRenameRequestDto.getUsername();
        var oldPath = fileRenameRequestDto.getPath();
        var newPath = fileRenameRequestDto.getNewPath();
        storageService.renameObject(username, oldPath, newPath);
        return "redirect:/storage";
    }

    @DeleteMapping
    public String deleteFile(@ModelAttribute("fileRequest") FileRequestDto fileRequestDto) {
        storageService.removeObject(fileRequestDto.getUsername(), fileRequestDto.getPath());
        return "redirect:/storage";
    }

    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<ByteArrayResource> downloadFile(@ModelAttribute("fileRequest") FileRequestDto fileRequestDto) {
        var fileName = fileRequestDto.getPath();
        var userName = fileRequestDto.getUsername();
        var file = storageService.downloadFile(userName, fileName);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + fileName)
                .body(file);
    }
}
