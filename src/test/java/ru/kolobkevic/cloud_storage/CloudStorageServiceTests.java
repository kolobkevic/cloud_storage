package ru.kolobkevic.cloud_storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kolobkevic.cloud_storage.exceptions.ObjectAlreadyExistsException;
import ru.kolobkevic.cloud_storage.exceptions.StorageObjectNotFoundException;
import ru.kolobkevic.cloud_storage.exceptions.StorageServerException;
import ru.kolobkevic.cloud_storage.services.StorageService;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Testcontainers
@SpringBootTest
class CloudStorageServiceTests {

    static String username = "test-user";

    @Autowired
    StorageService storageService;

    @Container
    private static MinIOContainer minIOContainer = new MinIOContainer("minio/minio:latest")
            .withUserName("minioadmin")
            .withPassword("minioadmin")
            .withExposedPorts(9000)
            .withCommand("server /data");

    @AfterEach
    void deleteTestFolder() throws StorageServerException {
        storageService.removeObject(username, "");
    }

    @Test
    void minIOContainerIsRunning() {
        Assertions.assertTrue(minIOContainer.isRunning());
    }

    @Test
    void folderFoundWhenCreated() throws StorageServerException, StorageObjectNotFoundException {
        storageService.createFolderList(username, "folder");
        Assertions.assertEquals(1, storageService.search(username, "folder").size());

        storageService.createFolderList(username, "folder2");
        Assertions.assertEquals(2, storageService.search(username, "folder").size());

        storageService.createFolderList(username, "new");
        Assertions.assertEquals(1, storageService.search(username, "new").size());

        storageService.createFolderList(username, "new/dir");
        Assertions.assertEquals(1, storageService.search(username, "dir").size());
    }

    @Test
    void objectsFoundWhenCreated() throws StorageServerException, StorageObjectNotFoundException, ObjectAlreadyExistsException {
        storageService.createFolderList(username, "apple/lemon");

        Assertions.assertEquals(1,
                storageService.search(username, "apple").size());

        Assertions.assertEquals(1,
                storageService.search(username, "lemon").size());
    }

    @Test
    void uploadFile() throws StorageServerException, StorageObjectNotFoundException {
        storageService.createFolderList(username, "apple/lemon/");
        storageService.uploadFile(username, List.of(mockFileWithName("123.txt")), "apple/lemon/");
        Assertions.assertEquals(1, storageService.search(username, "123.txt").size());
        Assertions.assertEquals(3,
                storageService.getListOfObjects(username, "apple", true).size());
    }

    @Test
    void uploadFolder() throws StorageServerException, StorageObjectNotFoundException {
        storageService.uploadFolder(username,
                List.of(mockFileWithName("apple/123.txt"),
                        mockFileWithName("apple/lemon/333.jpg")),
                "");
        Assertions.assertEquals(1, storageService.search(username, "123.txt").size());
        Assertions.assertEquals(4,
                storageService.getListOfObjects(username, "apple", true).size());
    }

    @Test
    void removeObject() {
    }

    @Test
    void renameObject() {
    }

    @Test
    void downloadFile() {
    }

    @Test
    void getBreadCrumb() {
    }

    private static MockMultipartFile mockFileWithName(String name) {
        return new MockMultipartFile(
                "mockFile",
                name,
                "text/plain",
                "Content".getBytes(StandardCharsets.UTF_8)
        );
    }
}