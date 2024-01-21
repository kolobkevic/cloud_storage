package ru.kolobkevic.cloud_storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kolobkevic.cloud_storage.exceptions.StorageServerException;
import ru.kolobkevic.cloud_storage.services.StorageService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@TestConfiguration(proxyBeanMethods = false)
@SpringBootTest
class CloudStorageTests {

    static String username = UUID.randomUUID().toString();

    @Autowired
    StorageService storageService;

    @Container
    private static MinIOContainer minIOContainer = new MinIOContainer("minio/minio:latest")
            .withUserName("minioadmin")
            .withPassword("minioadmin")
            .withExposedPorts(9000)
            .withCommand("server /data");


    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("minioURL", minIOContainer::getS3URL);
        registry.add("minio.endpoint", () -> minIOContainer.getHost() + minIOContainer.getMappedPort(9000));
        registry.add("minio.client.user", () -> minIOContainer.getUserName());
        registry.add("minio.client.password", () -> minIOContainer.getPassword());
        registry.add("minio.bucket-name", () -> username + "-files");
    }

    @Test
    void minIOContainerIsRunning() {
        Assertions.assertTrue(minIOContainer.isRunning());
    }

    @Test
    void folderFoundWhenCreated() throws StorageServerException {
        storageService.createFolder(username, "folder");
        assertEquals(1, storageService.search(username, "folder").size());

        storageService.createFolder(username, "folder2");
        assertEquals(2, storageService.search(username, "folder").size());

        storageService.createFolder(username, "new");
        assertEquals(1, storageService.search(username, "new").size());
    }
}
