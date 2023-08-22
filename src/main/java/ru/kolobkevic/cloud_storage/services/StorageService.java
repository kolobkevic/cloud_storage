package ru.kolobkevic.cloud_storage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kolobkevic.cloud_storage.repositories.MinioDAO;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final MinioDAO minioDAO;

    private List<File> getListOfObjects(String objectName) {
        return minioDAO.getListOfFiles(objectName);
    }
}
