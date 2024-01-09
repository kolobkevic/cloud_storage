package ru.kolobkevic.cloud_storage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kolobkevic.cloud_storage.dtos.BreadCrumbDto;
import ru.kolobkevic.cloud_storage.models.StorageObject;
import ru.kolobkevic.cloud_storage.repositories.StorageDAO;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final StorageDAO storageDAO;

    public void createUserFolder(String username) {
        storageDAO.createFolder(getUserFolderName(username));
    }

    public String getUserFolderName(String username) {
        return "user-" + username + "-files/";
    }

    public List<StorageObject> getListOfObjects(String username, String objectName, boolean isRecursive) {
        var objName = getUserFolderName(username) + objectName;
        var allObjects = storageDAO.getListOfObjects(objName, isRecursive);
        List<StorageObject> objects = new ArrayList<>();
        for (var obj : allObjects) {
            if (!obj.getPath().equals(objName) && !obj.getPath().equals(getUserFolderName(username))) {
                obj.setPath(getPathWithoutUsername(obj.getPath()));
                objects.add(obj);
            }
        }
        return objects;
    }

    private void uploadObject(String filePath, InputStream in) {
        storageDAO.uploadObject(filePath, in);
    }

    public void uploadFile(String username, List<MultipartFile> files, String path) {
        if (path.isEmpty()){
            path = getUserFolderName(username);
        }
        for (var file : files) {
            try (var stream = file.getInputStream()) {
                var objName = path + file.getOriginalFilename();
                uploadObject(objName, stream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadFolder(String username, MultipartFile[] files, String folderPath) {
        for (var file : files) {
            try (var stream = file.getInputStream()) {
                uploadObject(getUserFolderName(username) + folderPath, stream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void createFolder(String username, String folderName) {
        storageDAO.createFolder(getUserFolderName(username) + folderName);
    }

    public void removeObject(String username, String filePath) {
        storageDAO.removeObject(getUserFolderName(username) + getPathWithoutUsername(filePath));
    }

    public void renameObject(String username, String oldName, String newName) {
        if (oldName.endsWith("/")) {
            renameFolder(username, oldName, newName);
        } else {
            storageDAO.renameObject(oldName, getFileName(oldName, newName));
        }
    }

    public ByteArrayResource downloadFile(String username, String objectName) {
        return storageDAO.downloadObject(getUserFolderName(username) + getPathWithoutUsername(objectName));
    }

    public List<BreadCrumbDto> getBreadCrumb(String path) {
        var segments = Arrays.stream(path.split("/")).toList();
        var breadCrumbList = new ArrayList<BreadCrumbDto>();

        for (int i = 0; i < segments.size(); i++) {
            breadCrumbList.add(new BreadCrumbDto(segments.subList(0, i + 1)));
        }

        return path.isEmpty() ? Collections.emptyList() : breadCrumbList;
    }

    private String getPathWithoutUsername(String path) {
        return path.isEmpty() ? path : path.substring(path.indexOf('/') + 1);
    }

    private String getFileNameFromPath(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private String getFileName(String path, String newName) {
        return path.replace(getFileNameFromPath(path), newName);
    }

    private void renameFolder(String username, String oldName, String newName) {
        var objects = getListOfObjects(username, oldName, true);
        for (var obj : objects) {
            var newPath = obj.getPath().replace(oldName, getNewPath(oldName, newName));
            storageDAO.renameObject(obj.getPath(), newPath);
        }
    }

    private String getNewPath(String path, String newPath) {
        var splitted = path.split("/");
        splitted[splitted.length - 1] = newPath;
        return String.join("/", splitted) + "/";
    }
}
