package ru.kolobkevic.cloud_storage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kolobkevic.cloud_storage.dtos.BreadCrumbDto;
import ru.kolobkevic.cloud_storage.exceptions.ObjectAlreadyExistsException;
import ru.kolobkevic.cloud_storage.exceptions.StorageObjectNotFoundException;
import ru.kolobkevic.cloud_storage.exceptions.StorageServerException;
import ru.kolobkevic.cloud_storage.models.StorageObject;
import ru.kolobkevic.cloud_storage.repositories.StorageDAO;

import java.util.*;

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

    public void uploadFile(String username, List<MultipartFile> files, String path) throws StorageServerException {
        if (path.isEmpty()) {
            path = getUserFolderName(username);
        }
        for (var file : files) {
            try (var stream = file.getInputStream()) {
                var objName = path + file.getOriginalFilename();
                storageDAO.uploadObject(getUserFolderName(username) + objName, stream);
            } catch (Exception e) {
                throw new StorageServerException(e.getMessage());
            }
        }
    }

    public void uploadFolder(String username, List<MultipartFile> files, String path)
            throws StorageServerException {
        for (var file : files) {
            createFolderList(username, getParentPath(file.getOriginalFilename()));
            try (var stream = file.getInputStream()) {
                var objName = path + file.getOriginalFilename();
                storageDAO.uploadObject(getUserFolderName(username) + objName, stream);
            } catch (Exception e) {
                throw new StorageServerException(e.getMessage());
            }
        }
    }

    public void createFolderList(String username, String folderName) throws StorageServerException {
        if (!folderName.endsWith("/")) {
            folderName = folderName + "/";
        }
        var folderNames = folderName.split("/");
        StringBuilder name = new StringBuilder();
        for (var objectName : folderNames) {
            try {
                name.append(objectName).append("/");
                createFolder(username, name.toString());
            } catch (ObjectAlreadyExistsException ignore){
                
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

    public void removeObject(String username, String filePath) throws StorageServerException {
        storageDAO.removeObject(getUserFolderName(username) + filePath);
    }

    public void renameObject(String username, String oldName, String newName)
            throws ObjectAlreadyExistsException, StorageServerException, StorageObjectNotFoundException {
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

    public ByteArrayResource downloadFile(String username, String objectName) throws StorageServerException {
        return storageDAO.downloadObject(getUserFolderName(username) + objectName);
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
            storageDAO.renameObject(getUserFolderName(username) + obj.getPath(),
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
        storageDAO.renameObject(getUserFolderName(username) + oldName,
                getUserFolderName(username) + fullNewName);
    }

    private String getNewPath(String path, String newPath) {
        var splitted = path.split("/");
        splitted[splitted.length - 1] = newPath;
        return String.join("/", splitted) + "/";
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
