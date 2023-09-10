package ru.kolobkevic.cloud_storage.repositories.impl;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import ru.kolobkevic.cloud_storage.models.StorageObject;
import ru.kolobkevic.cloud_storage.repositories.StorageDAO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioDAO implements StorageDAO {
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;
    @Value("${url.schema}")
    private String schema;
    @Value("${url.port}")
    private String port;
    @Value("${url.host}")
    private String hostName;
    private static final int PART_SIZE = 104857600;

    private Iterable<Result<Item>> getObjects(String objectName, boolean isRecursive) {
        log.info("Getting list of objects with name " + objectName + " from bucket " + bucketName);
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(objectName)
                        .recursive(isRecursive)
                        .build());
    }

    @Override
    public List<StorageObject> getListOfObjects(String objectName, boolean isRecursive) {
        List<StorageObject> files = new ArrayList<>();
        var minioObjects = getObjects(objectName, false);

        try {
            for (var minioObject : minioObjects) {
                Item item = minioObject.get();
                String path = item.objectName();
                log.info("item.objectName: " + path);

                boolean isDir = item.isDir() || path.endsWith("/");
                log.info("item is directory: " + isDir);

                String displayName = getFolderName(path);
                log.info("displayName: " + displayName);

                files.add(new StorageObject(displayName, path, isDir));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    @Override
    public void uploadObject(String filePath, InputStream in) {
        log.info("Uploading objects with name " + filePath + " from bucket " + bucketName);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .stream(in, -1, PART_SIZE)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createFolder(String filePath) {
        log.info("Creating folder with name " + filePath + " from bucket " + bucketName);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void copyObject(String filePath, String newPath) {
        log.info("Copying objects from " + filePath + " to " + newPath + " from bucket " + bucketName);
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(newPath)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(filePath)
                                            .build())
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeObject(String filePath) {
        var objects = prepareForDelete(filePath);

        var deleteResults = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(objects)
                        .build());

        for (var obj : deleteResults) {
            try {
                var error = obj.get();
                log.warn("Error in deleting object " + error.objectName() + "; " + error.message());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void renameObject(String oldName, String newName) {
        copyObject(oldName, newName);
        removeObject(oldName);
    }

    @Override
    public String getObjectUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(1, TimeUnit.DAYS)
                            .extraQueryParams(new HashMap<>(
                                    Map.of("response-content-type", "application/octet-stream")))
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
            return "Could not get URL";
        }
    }

    @Override
    public ByteArrayResource downloadObject(String filePath) {
        GetObjectArgs objectArgs = GetObjectArgs.builder()
                .bucket(bucketName)
                .object(filePath)
                .build();
        try (var object = minioClient.getObject(objectArgs)) {
            return new ByteArrayResource(object.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private String getFolderName(String str) {
        var splitted = str.split("/");
        return splitted[splitted.length - 1];
    }

    private List<DeleteObject> prepareForDelete(String filePath) {
        var objects = getObjects(filePath, true);
        List<DeleteObject> objectsForDelete = new ArrayList<>();

        try {
            for (var obj : objects) {
                objectsForDelete.add(new DeleteObject(obj.get().objectName()));
            }
            return objectsForDelete;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private String getUrlForDirectory(String encodedSubDirectoryPath) {

        return UriComponentsBuilder
                .newInstance()
                .scheme(schema)
                .host(hostName)
                .port(port)
                .path("/")
                .queryParam("path", encodedSubDirectoryPath)
                .build()
                .toUriString();
    }
}
