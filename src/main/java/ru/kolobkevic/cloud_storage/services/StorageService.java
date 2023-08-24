package ru.kolobkevic.cloud_storage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kolobkevic.cloud_storage.models.StorageObject;
import ru.kolobkevic.cloud_storage.repositories.StorageDAO;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final StorageDAO storageDAO;

    public void createUserFolder(Long id) {
        storageDAO.createFolder(getUserFolderName(id));
    }

    public String getUserFolderName(Long id) {
        return "user-" + id + "-files/";
    }

    public List<StorageObject> getListOfObjects(String objectName) {
        return storageDAO.getListOfObjects(objectName);
    }

    private void uploadObject(String filePath, InputStream in) {
        storageDAO.uploadObject(filePath, in);
    }

    public void uploadFile(MultipartFile file, String filePath) throws IOException {
        uploadObject(filePath, file.getInputStream());
    }

    public void uploadFolder(MultipartFile[] files, String folderPath) throws IOException {
        for (var file : files) {
            uploadObject(folderPath, file.getInputStream());
        }
    }

    public void createFolder(String folderName){
        storageDAO.createFolder(folderName);
    }

    public void copyObject(String filePath, String newPath) {
        storageDAO.copyObject(filePath, newPath);
    }

    public void removeObject(String filePath) {
        storageDAO.removeObject(filePath);
    }

    public String getObjectUrl(String objectName) {
        return storageDAO.getObjectUrl(objectName);
    }
}
