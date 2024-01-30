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
import org.springframework.web.bind.annotation.RequestParam;
import ru.kolobkevic.cloud_storage.dtos.FilesDto;
import ru.kolobkevic.cloud_storage.dtos.FileRenameRequestDto;
import ru.kolobkevic.cloud_storage.dtos.FileRequestDto;
import ru.kolobkevic.cloud_storage.dtos.FolderRenameDto;
import ru.kolobkevic.cloud_storage.exceptions.ObjectAlreadyExistsException;
import ru.kolobkevic.cloud_storage.exceptions.StorageObjectNotFoundException;
import ru.kolobkevic.cloud_storage.exceptions.StorageServerException;
import ru.kolobkevic.cloud_storage.services.StorageService;
import ru.kolobkevic.cloud_storage.utils.RedirectUtils;

@Controller
@RequiredArgsConstructor

@Slf4j
public class StorageController {
    private final StorageService storageService;
    private final RedirectUtils redirectUtils;
    private static final String PAGE_REDIRECTION_PREFIX = "redirect:/storage?path=";

    @GetMapping("/storage")
    public String getFiles(@AuthenticationPrincipal User user,
                           @RequestParam(value = "path", required = false, defaultValue = "") String path,
                           Model model) throws StorageServerException, StorageObjectNotFoundException {
        log.info("Path: " + path);
        model.addAttribute("breadCrumbsList", storageService.getBreadCrumb(path));
        model.addAttribute("files", storageService.getListOfObjects(user.getUsername(),
                path, false));
        model.addAttribute("username", user.getUsername());
        model.addAttribute("path", path);

        return "cloud-storage";
    }

    @PostMapping("/storage/upload")
    public String uploadFile(@ModelAttribute("filesDto") FilesDto filesDto) throws StorageServerException {
        storageService.uploadFile(filesDto.getUsername(), filesDto.getFiles(), filesDto.getPath());
        return PAGE_REDIRECTION_PREFIX + redirectUtils.getRedirectPath(filesDto.getPath());

    }

    @PutMapping("/storage")
    public String renameFile(@ModelAttribute("fileRenameRequest") FileRenameRequestDto fileRenameRequestDto)
            throws ObjectAlreadyExistsException, StorageServerException, StorageObjectNotFoundException {
        var username = fileRenameRequestDto.getUsername();
        var oldPath = fileRenameRequestDto.getPath();
        var newPath = fileRenameRequestDto.getNewPath();
        storageService.renameObject(username, oldPath, newPath);
        var redirection = storageService.getParentPath(fileRenameRequestDto.getPath());
        return PAGE_REDIRECTION_PREFIX + redirectUtils.getRedirectPath(redirection);
    }

    @DeleteMapping("/storage")
    public String deleteFile(@ModelAttribute("fileRequest") FileRequestDto fileRequestDto)
            throws StorageServerException {
        storageService.removeObject(fileRequestDto.getUsername(), fileRequestDto.getPath());
        var redirection = storageService.getParentPath(fileRequestDto.getPath());
        return PAGE_REDIRECTION_PREFIX + redirectUtils.getRedirectPath(redirection);
    }

    @GetMapping(value = "/storage/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)

    public ResponseEntity<ByteArrayResource> downloadFile(@ModelAttribute("fileRequest") FileRequestDto fileRequestDto)
            throws StorageServerException {
        var objectName = fileRequestDto.getPath();
        var userName = fileRequestDto.getUsername();
        var filename = fileRequestDto.getObjectName();
        var file = storageService.downloadFile(userName, objectName);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .body(file);
    }

    @GetMapping("/search")
    public String search(@AuthenticationPrincipal User user,
                         @RequestParam("query") String query,
                         Model model) throws StorageServerException, StorageObjectNotFoundException {
        var results = storageService.search(user.getUsername(), query);
        model.addAttribute("searchResults", results);
        model.addAttribute("username", user.getUsername());
        return "search";
    }

    @PostMapping("/storage/create")
    public String createFolder(@ModelAttribute("fileRequest") FolderRenameDto folderRenameDto)
            throws StorageServerException, ObjectAlreadyExistsException {

        storageService.createFolder(folderRenameDto.getUsername(),
                folderRenameDto.getPath() + folderRenameDto.getNewPath());
        return PAGE_REDIRECTION_PREFIX + redirectUtils.getRedirectPath(folderRenameDto.getPath());
    }
}
