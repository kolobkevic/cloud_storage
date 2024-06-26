package ru.kolobkevic.cloud_storage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import ru.kolobkevic.cloud_storage.utils.BreadCrumb;
import ru.kolobkevic.cloud_storage.dtos.StorageObjDto;
import ru.kolobkevic.cloud_storage.dtos.StorageObjRenameDto;
import ru.kolobkevic.cloud_storage.dtos.FilesUploadDto;
import ru.kolobkevic.cloud_storage.exceptions.ObjectAlreadyExistsException;
import ru.kolobkevic.cloud_storage.exceptions.StorageObjectNotFoundException;
import ru.kolobkevic.cloud_storage.exceptions.StorageServerException;
import ru.kolobkevic.cloud_storage.models.StorageObject;
import ru.kolobkevic.cloud_storage.repositories.StorageS3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {
    private final StorageS3 storageS3;

    @Override
    public void createUserFolder(String username) throws StorageServerException {
        storageS3.createFolder(getUserFolderName(username));
    }

    @Override
    public String getUserFolderName(String username) {
        return "user-" + username + "-files/";
    }

    @Override
    public List<StorageObject> getListOfObjects(String username, String objectName, boolean isRecursive)
            throws StorageServerException, StorageObjectNotFoundException {
        var objName = getUserFolderName(username) + objectName;
        var allObjects = storageS3.getListOfObjects(objName, isRecursive);
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

    @Override
    public void uploadFile(FilesUploadDto filesUploadDto) throws StorageServerException {
        var username = filesUploadDto.getUsername();
        var files = filesUploadDto.getFiles();
        var path = filesUploadDto.getPath();
        for (var file : files) {
            try (var stream = file.getInputStream()) {
                var objName = path + file.getOriginalFilename();
                storageS3.uploadObject(getUserFolderName(username) + objName, stream);
            } catch (Exception e) {
                throw new StorageServerException(e.getMessage());
            }
        }
    }

    @Override
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
            createFolder(username, path + getParentPath(fileName));
            try (var stream = file.getInputStream()) {
                var objName = path + file.getOriginalFilename();
                storageS3.uploadObject(getUserFolderName(username) + objName, stream);
            } catch (Exception e) {
                throw new StorageServerException(e.getMessage());
            }
        }
    }

    @Override
    public void removeObject(StorageObjDto storageObjDto) throws StorageServerException {
        storageS3.removeObject(getUserFolderName(storageObjDto.getUsername()) + storageObjDto.getPath());
    }

    @Override
    public void renameObject(StorageObjRenameDto storageObjectRenameDto)
            throws ObjectAlreadyExistsException, StorageServerException, StorageObjectNotFoundException {
        var username = storageObjectRenameDto.getUsername();
        var oldName = storageObjectRenameDto.getPath();
        var newName = storageObjectRenameDto.getNewPath();

        if (oldName.endsWith("/")) {
            renameFolder(username, oldName, newName);
        } else {
            renameFile(username, oldName, newName);
        }
    }

    @Override
    public ByteArrayResource downloadFile(StorageObjDto storageObjDto) throws StorageServerException {
        return storageS3.downloadObject(
                getUserFolderName(storageObjDto.getUsername()) + storageObjDto.getPath());
    }

    @Override
    public List<BreadCrumb> getBreadCrumb(String path) {
        var segments = Arrays.stream(path.split("/")).toList();
        var breadCrumbList = new ArrayList<BreadCrumb>();

        for (int i = 0; i < segments.size(); i++) {
            breadCrumbList.add(new BreadCrumb(segments.subList(0, i + 1)));
        }

        return path.isEmpty() ? Collections.emptyList() : breadCrumbList;
    }

    @Override
    public List<StorageObject> search(String username, String query) throws
            StorageServerException, StorageObjectNotFoundException {
        var objects = getListOfObjects(username, "", true);
        return objects.stream()
                .filter(obj -> obj.getObjectName().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    @Override
    public String getParentPath(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.lastIndexOf('/'));
        }
        return path.substring(0, path.lastIndexOf('/') + 1);
    }

    @Override
    public void createFolder(String username, String folderName) throws StorageServerException,
            ObjectAlreadyExistsException {

        if (!folderName.endsWith("/")) {
            folderName = folderName + "/";
        }
        var folderNames = Arrays.stream(folderName.split("/")).skip(0).toList();
        StringBuilder name = new StringBuilder();
        for (var objectName : folderNames) {
            name.append(objectName).append("/");
            if (!isObjectExists(username, name.toString())) {
                storageS3.createFolder(getUserFolderName(username) + name);
            }
        }

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

    private String getNewPath(String path, String newPath) {
        var splitted = path.split("/");
        splitted[splitted.length - 1] = newPath;
        if (newPath.endsWith("/")) {
            return String.join("/", splitted);
        } else {
            return String.join("/", splitted) + "/";
        }
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
            storageS3.copyObject(getUserFolderName(username) + obj.getPath(),
                    getUserFolderName(username) + newPath);
        }
        storageS3.removeObject(getUserFolderName(username) + oldName);
    }

    private void renameFile(String username, String oldName, String newName) throws StorageServerException,
            ObjectAlreadyExistsException {

        var fullNewName = renameFileName(oldName, newName);
        if (isObjectExists(username, fullNewName)) {
            throw new ObjectAlreadyExistsException("Объект с таким именем уже существует");
        }
        storageS3.copyObject(getUserFolderName(username) + oldName,
                getUserFolderName(username) + fullNewName);
        storageS3.removeObject(getUserFolderName(username) + oldName);
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
}
