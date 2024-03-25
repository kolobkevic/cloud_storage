package ru.kolobkevic.cloud_storage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import ru.kolobkevic.cloud_storage.dtos.BreadCrumbDto;
import ru.kolobkevic.cloud_storage.dtos.FileDto;
import ru.kolobkevic.cloud_storage.dtos.FileRenameDto;
import ru.kolobkevic.cloud_storage.dtos.FilesUploadDto;
import ru.kolobkevic.cloud_storage.exceptions.ObjectAlreadyExistsException;
import ru.kolobkevic.cloud_storage.exceptions.StorageObjectNotFoundException;
import ru.kolobkevic.cloud_storage.exceptions.StorageServerException;
import ru.kolobkevic.cloud_storage.models.StorageObject;
import ru.kolobkevic.cloud_storage.repositories.StorageDAO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final StorageDAO storageDAO;

    public void createUserFolder(String username) throws StorageServerException {
        storageDAO.createFolder(getUserFolderName(username));
    }

    public String getUserFolderName(String username) {
        return "user-" + username + "-files/";
    }

    public List<StorageObject> getListOfObjects(String username, String objectName, boolean isRecursive)
            throws StorageServerException, StorageObjectNotFoundException {
        var objName = getUserFolderName(username) + objectName;
        var allObjects = storageDAO.getListOfObjects(objName, isRecursive);
        List<StorageObject> objects = new ArrayList<>();
        for (var obj : allObjects) {
            if (obj.isDir()) {
                if (!obj.getPath().equals(objName) && !obj.getPath().equals(getUserFolderName(username))) {
                    obj.setPath(getPathWithoutUsername(obj.getPath()));
                    objects.add(obj);
                }
            } else {
                obj.setPath(getPathWithoutUsername(obj.getPath()));
                objects.add(obj);
            }
        }
        return objects;
    }

    public void uploadFile(FilesUploadDto filesUploadDto) throws StorageServerException {
        var username = filesUploadDto.getUsername();
        var files = filesUploadDto.getFiles();
        var path = filesUploadDto.getPath();
        for (var file : files) {
            try (var stream = file.getInputStream()) {
                var objName = path + file.getOriginalFilename();
                storageDAO.uploadObject(getUserFolderName(username) + objName, stream);
            } catch (Exception e) {
                throw new StorageServerException(e.getMessage());
            }
        }
    }

    public void uploadFolder(FilesUploadDto filesUploadDto)
            throws StorageServerException, ObjectAlreadyExistsException {
        var username = filesUploadDto.getUsername();
        var files = filesUploadDto.getFiles();
        var path = filesUploadDto.getPath();

        for (var file : files) {
            var fileName = file.getOriginalFilename();
            if (fileName == null) {
                fileName = "";
            }
            createFolderList(username, path + getParentPath(fileName));
            try (var stream = file.getInputStream()) {
                var objName = path + file.getOriginalFilename();
                storageDAO.uploadObject(getUserFolderName(username) + objName, stream);
            } catch (Exception e) {
                throw new StorageServerException(e.getMessage());
            }
        }
    }

    public void createFolderList(String username, String folderName) throws StorageServerException,
            ObjectAlreadyExistsException {
        if (!folderName.endsWith("/")) {
            folderName = folderName + "/";
        }
        var folderNames = Arrays.stream(folderName.split("/")).skip(0).toList();
        StringBuilder name = new StringBuilder();
        for (var objectName : folderNames) {
            name.append(objectName).append("/");
            if (!isObjectExists(username, name.toString())) {
                createFolder(username, name.toString());
            }
        }
    }

    private void createFolder(String username, String folderName) throws StorageServerException,
            ObjectAlreadyExistsException {
        folderName = folderName.endsWith("/") ? folderName : (folderName + "/");
        if (isObjectExists(username, folderName)) {
            throw new ObjectAlreadyExistsException("Объект с именем " + folderName + " уже существует");
        }
        storageDAO.createFolder(getUserFolderName(username) + folderName);
    }

    public void removeObject(FileDto fileDto) throws StorageServerException {
        storageDAO.removeObject(getUserFolderName(fileDto.getUsername()) + fileDto.getPath());
    }

    public void renameObject(FileRenameDto fileRenameDto)
            throws ObjectAlreadyExistsException, StorageServerException, StorageObjectNotFoundException {
        var username = fileRenameDto.getUsername();
        var oldName = fileRenameDto.getPath();
        var newName = fileRenameDto.getNewPath();

        if (oldName.endsWith("/")) {
            renameFolder(username, oldName, newName);
        } else {
            renameFile(username, oldName, newName);
        }
    }

    private boolean isObjectExists(String username, String filename)
            throws StorageServerException {
        List<StorageObject> objects;
        try {
            objects = getListOfObjects(username, filename, false);
        } catch (StorageObjectNotFoundException e) {
            return false;
        }
        return !objects.isEmpty();
    }

    public ByteArrayResource downloadFile(FileDto fileDto) throws StorageServerException {
        return storageDAO.downloadObject(getUserFolderName(fileDto.getUsername()) + fileDto.getPath());
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

    private String renameFileName(String path, String newName) {
        return path.replace(getFileNameFromPath(path), newName);
    }

    private void renameFolder(String username, String oldName, String newName) throws StorageServerException,
            StorageObjectNotFoundException, ObjectAlreadyExistsException {

        var fullNewName = getNewPath(oldName, newName);
        if (isObjectExists(username, fullNewName)) {
            throw new ObjectAlreadyExistsException("Объект с таким именем уже существует");
        }
        createFolder(username, fullNewName);

        var objects = getListOfObjects(username, oldName, true);
        for (var obj : objects) {
            var newPath = obj.getPath().replace(oldName, fullNewName);
            storageDAO.copyObject(getUserFolderName(username) + obj.getPath(),
                    getUserFolderName(username) + newPath);
        }
        storageDAO.removeObject(getUserFolderName(username) + oldName);
    }

    private void renameFile(String username, String oldName, String newName) throws StorageServerException,
            ObjectAlreadyExistsException {

        var fullNewName = renameFileName(oldName, newName);
        if (isObjectExists(username, fullNewName)) {
            throw new ObjectAlreadyExistsException("Объект с таким именем уже существует");
        }
        storageDAO.copyObject(getUserFolderName(username) + oldName,
                getUserFolderName(username) + fullNewName);
        storageDAO.removeObject(getUserFolderName(username) + oldName);
    }

    private String getNewPath(String path, String newPath) {
        var splitted = path.split("/");
        splitted[splitted.length - 1] = newPath;
        if (newPath.endsWith("/")) {
            return String.join("/", splitted);
        } else {
            return String.join("/", splitted) + "/";
        }
    }

    public List<StorageObject> search(String username, String query) throws
            StorageServerException, StorageObjectNotFoundException {
        var objects = getListOfObjects(username, "", true);
        return objects.stream()
                .filter(obj -> obj.getObjectName().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    public String getParentPath(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.lastIndexOf('/'));
        }
        return path.substring(0, path.lastIndexOf('/') + 1);
    }
}
