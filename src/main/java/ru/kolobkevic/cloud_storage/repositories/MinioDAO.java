package ru.kolobkevic.cloud_storage.repositories;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class MinioDAO {
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;
    @Value("${url.schema}")
    private String schema;
    @Value("${url.port}")
    private String port;
    @Value("${url.hostName}")
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

    public List<File> getListOfFiles(String objectName) {
        List<File> files = new ArrayList<>();
        var minioObjects = getObjects(objectName);

        try {
            for (var minioObject : minioObjects) {
                Item item = minioObject.get();

                String displayName = getFileName(item.objectName());

                if (!displayName.isEmpty()) {

                    String path = getPathWithoutRoot(item.objectName());
                    String currentDirectoryPath = getParentPath(path);
                    String encodedCurrentDirectoryPath = URLEncoder.encode(currentDirectoryPath, StandardCharsets.UTF_8);
                    String urlCurEncoded = getUrlForDirectory(encodedCurrentDirectoryPath);

                    // Get URL of file or subdirectory
                    String urlSubEncoded;

                    if (isDirectory(item.objectName())) {
                        String encodedSubDirectoryPath = URLEncoder.encode(path, StandardCharsets.UTF_8.toString());
                        urlSubEncoded = getUrlForDirectory(encodedSubDirectoryPath);
                    } else {
                        urlSubEncoded = getPreSignedObjectUrl(bucketName, item.objectName());
                    }

                    // Create file-info with params. idOnPage needs for correct Thymeleaf foreach loop in modal form
//                files.add(new File(displayName, item.objectName(), urlSubEncoded, urlCurEncoded));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    public void putObject(String filePath, InputStream in) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        log.info("Uploading objects with name " + filePath + " from bucket " + bucketName);
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(filePath)
                .stream(in, -1, PART_SIZE)
                .build());
    }

    public void createFolder(String filePath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        log.info("Creating folder with name " + filePath + " from bucket " + bucketName);
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(filePath)
                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                .build());
    }

    public void copyObject(String filePath, String newPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        log.info("Copying objects from " + filePath + " to " + newPath + " from bucket " + bucketName);
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
    }

    public void removeObject(String filePath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filePath)
                        .build());
    }

    private String getPathWithoutRoot(String str) {
        Path path = Paths.get(str);
        return path.subpath(1, path.getNameCount() - 1).toString();
    }

    private String getParentPath(String str) {
        Path path = Paths.get(str);
        return isDirectory(str) ? path.getParent().toString() : path.getParent().toString() + "/";
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

    public String getPreSignedObjectUrl(String bucketName, String objectName)
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
            InternalException {

        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(2, TimeUnit.HOURS)
                        .extraQueryParams(new HashMap<>(
                                Map.of("response-content-type", "application/octet-stream")))
                        .build());
    }
}
