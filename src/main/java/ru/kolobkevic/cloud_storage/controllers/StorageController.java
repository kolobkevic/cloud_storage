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
import ru.kolobkevic.cloud_storage.dtos.FilesUploadDto;
import ru.kolobkevic.cloud_storage.dtos.FileRenameDto;
import ru.kolobkevic.cloud_storage.dtos.FileDto;
import ru.kolobkevic.cloud_storage.dtos.FolderRenameDto;
import ru.kolobkevic.cloud_storage.exceptions.ObjectAlreadyExistsException;
import ru.kolobkevic.cloud_storage.exceptions.StorageObjectNotFoundException;
import ru.kolobkevic.cloud_storage.exceptions.StorageServerException;
import ru.kolobkevic.cloud_storage.services.StorageService;
import ru.kolobkevic.cloud_storage.utils.RedirectUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
    public String uploadFile(@ModelAttribute("filesDto") FilesUploadDto filesUploadDto) throws StorageServerException {
        storageService.uploadFile(filesUploadDto);
        return PAGE_REDIRECTION_PREFIX + redirectUtils.getRedirectPath(filesUploadDto.getPath());
    }

    @PostMapping("/storage/uploadFolder")
    public String uploadFolder(@ModelAttribute("filesDto") FilesUploadDto filesUploadDto) throws StorageServerException,
            ObjectAlreadyExistsException {

        storageService.uploadFolder(filesUploadDto);
        return PAGE_REDIRECTION_PREFIX + redirectUtils.getRedirectPath(filesUploadDto.getPath());
    }

    @PutMapping("/storage")
    public String renameFile(@ModelAttribute("fileRenameRequest") FileRenameDto fileRenameDto)
            throws ObjectAlreadyExistsException, StorageServerException, StorageObjectNotFoundException {

        storageService.renameObject(fileRenameDto);
        var redirection = storageService.getParentPath(fileRenameDto.getPath());
        return PAGE_REDIRECTION_PREFIX + redirectUtils.getRedirectPath(redirection);
    }

    @DeleteMapping("/storage")
    public String deleteFile(@ModelAttribute("fileRequest") FileDto fileDto)
            throws StorageServerException {

        storageService.removeObject(fileDto);
        var redirection = storageService.getParentPath(fileDto.getPath());
        return PAGE_REDIRECTION_PREFIX + redirectUtils.getRedirectPath(redirection);
    }

    @GetMapping(value = "/storage/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)

    public ResponseEntity<ByteArrayResource> downloadFile(@ModelAttribute("fileRequest") FileDto fileDto)
            throws StorageServerException {
        var filename = URLEncoder.encode(fileDto.getObjectName(), StandardCharsets.UTF_8);
        var file = storageService.downloadFile(fileDto);
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

        storageService.createFolderList(folderRenameDto.getUsername(),
                folderRenameDto.getPath() + folderRenameDto.getNewPath());
        return PAGE_REDIRECTION_PREFIX + redirectUtils.getRedirectPath(folderRenameDto.getPath());
    }
}
