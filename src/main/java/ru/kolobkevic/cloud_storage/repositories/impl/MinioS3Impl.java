package ru.kolobkevic.cloud_storage.repositories.impl;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import ru.kolobkevic.cloud_storage.configs.MinioProperties;
import ru.kolobkevic.cloud_storage.exceptions.StorageObjectNotFoundException;
import ru.kolobkevic.cloud_storage.exceptions.StorageServerException;
import ru.kolobkevic.cloud_storage.models.StorageObject;
import ru.kolobkevic.cloud_storage.repositories.StorageS3;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioS3Impl implements StorageS3 {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    private static final int PART_SIZE = 104857600;

    private Iterable<Result<Item>> getObjects(String objectName, boolean isRecursive) {
        log.debug("Getting list of objects with name {} from bucket {}", objectName, minioProperties.getBucket());
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .prefix(objectName)
                        .recursive(isRecursive)
                        .build());
    }

    @Override
    public List<StorageObject> getListOfObjects(String objectName, boolean isRecursive) throws StorageServerException, StorageObjectNotFoundException {
        List<StorageObject> files = new ArrayList<>();
        var minioObjects = getObjects(objectName, isRecursive);

        if(!minioObjects.iterator().hasNext()){
            throw new StorageObjectNotFoundException("No such file or folder");
        }

        try {
            for (var minioObject : minioObjects) {
                Item item = minioObject.get();
                String path = item.objectName();
                log.debug("item.objectName: {}", path);

                boolean isDir = item.isDir() || path.endsWith("/");
                log.debug("item is directory: {}", isDir);

                String displayName = getFolderName(path);
                log.debug("displayName: {}", displayName);

                files.add(new StorageObject(displayName, path, isDir));
            }
        } catch (Exception e) {
            throw new StorageServerException(e.getMessage());
        }
        return files;
    }

    @Override
    public void uploadObject(String filePath, InputStream in) throws StorageServerException {
        log.debug("Uploading objects with name {} from bucket {}", filePath, minioProperties.getBucket());
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(filePath)
                    .stream(in, -1, PART_SIZE)
                    .build());
        } catch (Exception e) {
            throw new StorageServerException(e.getMessage());
        }
    }

    @Override
    public void createFolder(String filePath) throws StorageServerException {
        log.debug("Creating folder with name {} from bucket {}", filePath, minioProperties.getBucket());
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(filePath)
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new StorageServerException(e.getMessage());
        }
    }

    @Override
    public void copyObject(String filePath, String newPath) throws StorageServerException {
        log.debug("Copying objects from {} to {} from bucket {}", filePath, newPath, minioProperties.getBucket());
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(newPath)
                            .source(
                                    CopySource.builder()
                                            .bucket(minioProperties.getBucket())
                                            .object(filePath)
                                            .build())
                            .build());
        } catch (Exception e) {
            throw new StorageServerException(e.getMessage());
        }
    }

    @Override
    public void removeObject(String filePath) throws StorageServerException {
        var objects = prepareForDelete(filePath);

        var deleteResults = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .objects(objects)
                        .build());

        for (var obj : deleteResults) {
            try {
                var error = obj.get();
                log.debug("Error in deleting object {}; {}", error.objectName(), error.message());
            } catch (Exception e) {
                throw new StorageServerException(e.getMessage());
            }
        }
    }

    @Override
    public ByteArrayResource downloadObject(String filePath) throws StorageServerException {
        GetObjectArgs objectArgs = GetObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .object(filePath)
                .build();
        try (var object = minioClient.getObject(objectArgs)) {
            return new ByteArrayResource(object.readAllBytes());
        } catch (Exception e) {
            throw new StorageServerException(e.getMessage());
        }
    }

    private String getFolderName(String str) {
        var splitted = str.split("/");
        return splitted[splitted.length - 1];
    }

    private List<DeleteObject> prepareForDelete(String filePath) throws StorageServerException {
        var objects = getObjects(filePath, true);
        List<DeleteObject> objectsForDelete = new ArrayList<>();

        try {
            for (var obj : objects) {
                objectsForDelete.add(new DeleteObject(obj.get().objectName()));
            }
            return objectsForDelete;
        } catch (Exception e) {
            throw new StorageServerException(e.getMessage());
        }
    }
}
