package ru.kolobkevic.cloud_storage.repositories;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import ru.kolobkevic.cloud_storage.exceptions.StorageServerException;
import ru.kolobkevic.cloud_storage.models.StorageObject;

import java.io.InputStream;
import java.util.List;

@Component
public interface StorageDAO {
    List<StorageObject> getListOfObjects(String objectName, boolean isRecursive) throws StorageServerException;

    void uploadObject(String filePath, InputStream in) throws StorageServerException;

    void createFolder(String filePath) throws StorageServerException;

    void copyObject(String filePath, String newPath) throws StorageServerException;

    void removeObject(String filePath) throws StorageServerException;

    void renameObject(String oldName, String newName) throws StorageServerException;

    ByteArrayResource downloadObject(String filePath) throws StorageServerException;
}
