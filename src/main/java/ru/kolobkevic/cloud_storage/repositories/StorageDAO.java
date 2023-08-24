package ru.kolobkevic.cloud_storage.repositories;

import org.springframework.stereotype.Component;
import ru.kolobkevic.cloud_storage.models.StorageObject;

import java.io.InputStream;
import java.util.List;

@Component
public interface StorageDAO {
    List<StorageObject> getListOfObjects(String objectName);

    void uploadObject(String filePath, InputStream in);

    void createFolder(String filePath);

    void copyObject(String filePath, String newPath);

    void removeObject(String filePath);

    String getObjectUrl(String objectName);
}
