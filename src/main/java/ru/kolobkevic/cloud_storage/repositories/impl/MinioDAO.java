package ru.kolobkevic.cloud_storage.repositories.impl;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import ru.kolobkevic.cloud_storage.models.StorageObject;
import ru.kolobkevic.cloud_storage.repositories.StorageDAO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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

    private Iterable<Result<Item>> getObjects(String objectName) {
        log.info("Getting list of objects with name " + objectName + " from bucket " + bucketName);
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(objectName)
                        .build());
    }

    @Override
    public List<StorageObject> getListOfObjects(String objectName) {
        List<StorageObject> files = new ArrayList<>();
        var minioObjects = getObjects(objectName);

        try {
            for (var minioObject : minioObjects) {
                Item item = minioObject.get();
                String path = item.objectName();
                log.info("item.objectName: " + path);

                String displayName = getFileName(path);
                log.info("displayName: " + displayName);

                boolean isDir = isDirectory(path);
                String url;

                if (!displayName.isEmpty()) {
                    url = isDir ?
                            getUrlForDirectory(URLEncoder.encode(displayName, StandardCharsets.UTF_8)) :
                            getObjectUrl(path);
                    files.add(new StorageObject(displayName, path, url, isDir));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("File 1: " + files.get(0).toString());
        log.info("File 2: " + files.get(1).toString());
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
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isDirectory(String str) {
        return str.endsWith("/");
    }

    private String getFileName(String str) {
        return Paths.get(str).getFileName().toString();
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
}
