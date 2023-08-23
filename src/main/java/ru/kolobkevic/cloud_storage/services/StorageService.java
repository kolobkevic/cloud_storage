package ru.kolobkevic.cloud_storage.services;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kolobkevic.cloud_storage.models.StorageObject;
import ru.kolobkevic.cloud_storage.repositories.MinioDAO;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final MinioDAO minioDAO;

    public void createUserFolder(Long id) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioDAO.createFolder(getUserFolderName(id));
    }

    public String getUserFolderName(Long id) {
        return "user-" + id + "-files/";
    }

    public List<StorageObject> getListOfObjects(String objectName) {
        return minioDAO.getListOfFiles(objectName);
    }
}
