package ru.kolobkevic.cloud_storage.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kolobkevic.cloud_storage.dtos.StorageObjDto;
import ru.kolobkevic.cloud_storage.dtos.StorageObjRenameDto;
import ru.kolobkevic.cloud_storage.dtos.FilesUploadDto;
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
        model.addAttribute("StorageObjectDto", new StorageObjDto());

        return "cloud-storage";
    }

    @PostMapping("/storage/upload")
    public String uploadFiles(@ModelAttribute("filesDto") FilesUploadDto filesUploadDto) throws StorageServerException {
        storageService.uploadFile(filesUploadDto);
        return PAGE_REDIRECTION_PREFIX + redirectUtils.getRedirectPath(filesUploadDto.getPath());
    }

    @PutMapping("/storage")
    public String renameObject(@ModelAttribute("StorageObjectRenameRequest")
                               @Valid StorageObjRenameDto storageObjectRenameDto,
                               BindingResult bindingResult, RedirectAttributes redirectAttributes)
            throws ObjectAlreadyExistsException, StorageServerException, StorageObjectNotFoundException {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("failureAlert",
                    bindingResult.getFieldError().getDefaultMessage());
        } else {
            storageService.renameObject(storageObjectRenameDto);
        }
        var redirection = storageService.getParentPath(storageObjectRenameDto.getPath());
        return PAGE_REDIRECTION_PREFIX + redirectUtils.getRedirectPath(redirection);
    }

    @DeleteMapping("/storage")
    public String deleteObject(@ModelAttribute("StorageObject") StorageObjDto storageObjDto)
            throws StorageServerException {

        storageService.removeObject(storageObjDto);
        var redirection = storageService.getParentPath(storageObjDto.getPath());
        return PAGE_REDIRECTION_PREFIX + redirectUtils.getRedirectPath(redirection);
    }

    @GetMapping(value = "/storage/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)

    public ResponseEntity<ByteArrayResource> downloadFile(@ModelAttribute("StorageObject") StorageObjDto storageObjDto)
            throws StorageServerException {
        var filename = URLEncoder.encode(storageObjDto.getObjectName(), StandardCharsets.UTF_8);
        var file = storageService.downloadFile(storageObjDto);
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
    public String createFolder(@ModelAttribute("folderDto") @Valid StorageObjDto storageObjDto,
                               BindingResult bindingResult, RedirectAttributes redirectAttributes)
            throws StorageServerException, ObjectAlreadyExistsException {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("failureAlert",
                    bindingResult.getFieldError().getDefaultMessage());
        } else {
            storageService.createFolderList(storageObjDto.getUsername(),
                    storageObjDto.getPath() + storageObjDto.getObjectName());
        }
        return PAGE_REDIRECTION_PREFIX + redirectUtils.getRedirectPath(storageObjDto.getPath());
    }
}
