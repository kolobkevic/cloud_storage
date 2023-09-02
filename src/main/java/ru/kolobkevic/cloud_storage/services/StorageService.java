package ru.kolobkevic.cloud_storage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kolobkevic.cloud_storage.models.StorageObject;
import ru.kolobkevic.cloud_storage.repositories.StorageDAO;

import java.io.InputStream;
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

    public List<StorageObject> getListOfObjects(String username, String objectName) {
        return storageDAO.getListOfObjects(getUserFolderName(username) + objectName);
    }

    private void uploadObject(String filePath, InputStream in) {
        storageDAO.uploadObject(filePath, in);
    }

    public void uploadFile(String username, MultipartFile file, String filePath) {
        try (var stream = file.getInputStream()) {
            uploadObject(getUserFolderName(username) + filePath, stream);
        } catch (Exception e) {
            e.printStackTrace();
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

    public void copyObject(String username, String filePath, String newPath) {
        storageDAO.copyObject(getUserFolderName(username) + filePath,
                getUserFolderName(username) + newPath);
    }

    public void removeObject(String username, String filePath) {
        storageDAO.removeObject(getUserFolderName(username) + filePath);
    }

    public String getObjectUrl(String username, String objectName) {
        return storageDAO.getObjectUrl(getUserFolderName(username) + objectName);
    }

    public void renameObject(String username, String oldName, String newName) {
        storageDAO.renameObject(
                getUserFolderName(username) + oldName,
                getUserFolderName(username) + newName);
    }

    public ByteArrayResource downloadFile(String username, String objectName) {
        return storageDAO.downloadObject(getUserFolderName(username) + objectName);
    }

    public List<String> getBreadCrumb(String path) {
        return path.isEmpty() ? Collections.emptyList() : List.of(path.split("/"));
    }
}
