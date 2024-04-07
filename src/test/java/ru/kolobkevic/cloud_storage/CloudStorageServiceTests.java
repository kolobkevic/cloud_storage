package ru.kolobkevic.cloud_storage;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kolobkevic.cloud_storage.dtos.StorageObjDto;
import ru.kolobkevic.cloud_storage.dtos.StorageObjRenameDto;
import ru.kolobkevic.cloud_storage.dtos.FilesUploadDto;
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

    FilesUploadDto file, folderWithFiles_1, folderWithFiles_2, folderWithFiles_3, folderWithFiles_4;
    StorageObjRenameDto obj_1, obj_2, obj_3;

    @Container
    private static MinIOContainer minIOContainer = new MinIOContainer("minio/minio:latest")
            .withUserName("minioadmin")
            .withPassword("minioadmin")
            .withExposedPorts(9000)
            .withCommand("server /data");

    @BeforeEach
    void init() throws StorageServerException {
        storageService.createUserFolder(username);
        file = new FilesUploadDto(List.of(mockFileWithName("123.txt")), username, "apple/lemon/");
        folderWithFiles_1 = new FilesUploadDto(List.of(mockFileWithName("apple/123.txt"),
                mockFileWithName("apple/lemon/333.jpg")), username, "");
        folderWithFiles_2 = new FilesUploadDto(List.of(mockFileWithName("apple/123.txt")),
                username, "");
        folderWithFiles_3 = new FilesUploadDto(List.of(mockFileWithName("apple/123.txt")),
                username, "");
        folderWithFiles_4 = new FilesUploadDto(List.of(mockFileWithName("456.txt")),
                username, "");

        obj_1 = new StorageObjRenameDto();
        obj_1.setPath("apple/123.txt");
        obj_1.setNewPath("333.txt");
        obj_1.setUsername(username);

        obj_2 = new StorageObjRenameDto();
        obj_2.setPath("456.txt");
        obj_2.setNewPath("007.txt");
        obj_2.setUsername(username);

        obj_3 = new StorageObjRenameDto();
        obj_3.setPath("apple/");
        obj_3.setNewPath("lemon/");
        obj_3.setUsername(username);


    }

    @AfterEach
    void deleteTestFolder() throws StorageServerException {
        storageService.removeObject(new StorageObjDto("", "", username));
    }

    @Test
    void minIOContainerIsRunning() {
        Assertions.assertTrue(minIOContainer.isRunning());
    }

    @Test
    void folderFoundWhenCreated() throws StorageServerException, StorageObjectNotFoundException,
            ObjectAlreadyExistsException {

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
    void objectsFoundWhenCreated() throws StorageServerException, StorageObjectNotFoundException,
            ObjectAlreadyExistsException {
        storageService.createUserFolder(username);

        storageService.createFolderList(username, "apple/lemon");

        Assertions.assertEquals(1,
                storageService.search(username, "apple").size());

        Assertions.assertEquals(1,
                storageService.search(username, "lemon").size());
    }

    @Test
    void uploadFile() throws StorageServerException, StorageObjectNotFoundException, ObjectAlreadyExistsException {
        storageService.createFolderList(username, "apple/lemon/");
        storageService.uploadFile(file);
        Assertions.assertEquals(1, storageService.search(username, "123.txt").size());
        Assertions.assertEquals(3,
                storageService.getListOfObjects(username, "apple", true).size());
    }

    @Test
    void uploadFolder() throws StorageServerException, StorageObjectNotFoundException, ObjectAlreadyExistsException {
        storageService.uploadFolder(folderWithFiles_1);
        Assertions.assertEquals(1, storageService.search(username, "123.txt").size());
        Assertions.assertEquals(4,
                storageService.getListOfObjects(username, "apple", true).size());
    }

    @Test
    void removeObject() throws StorageServerException, StorageObjectNotFoundException, ObjectAlreadyExistsException {
        storageService.uploadFolder(folderWithFiles_2);
        storageService.removeObject(new StorageObjDto("", "apple/", username));
        Assertions.assertEquals(0, storageService.search(username, "123").size());
        Assertions.assertEquals(0, storageService.search(username, "apple").size());
    }

    @Test
    void renameObject() throws StorageServerException, StorageObjectNotFoundException, ObjectAlreadyExistsException {
        storageService.uploadFolder(folderWithFiles_3);
        storageService.uploadFolder(folderWithFiles_4);
        storageService.renameObject(obj_1);
        storageService.renameObject(obj_2);
        Assertions.assertEquals(1, storageService.search(username, "333.txt").size());
        Assertions.assertEquals(1, storageService.search(username, "007.txt").size());

        storageService.renameObject(obj_3);
        Assertions.assertEquals(0, storageService.search(username, "apple").size());
        Assertions.assertEquals(0, storageService.search(username, "123").size());
        Assertions.assertEquals(0, storageService.search(username, "456").size());
        Assertions.assertEquals(1, storageService.search(username, "lemon").size());
        Assertions.assertEquals(2,
                storageService.getListOfObjects(username, "lemon", true).size());
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
