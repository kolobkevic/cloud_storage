package ru.kolobkevic.cloud_storage.services;

import org.springframework.core.io.ByteArrayResource;
import ru.kolobkevic.cloud_storage.dtos.FilesUploadDto;
import ru.kolobkevic.cloud_storage.dtos.StorageObjDto;
import ru.kolobkevic.cloud_storage.dtos.StorageObjRenameDto;
import ru.kolobkevic.cloud_storage.exceptions.ObjectAlreadyExistsException;
import ru.kolobkevic.cloud_storage.exceptions.StorageObjectNotFoundException;
import ru.kolobkevic.cloud_storage.exceptions.StorageServerException;
import ru.kolobkevic.cloud_storage.models.StorageObject;
import ru.kolobkevic.cloud_storage.utils.BreadCrumb;

import java.util.List;

public interface StorageService {

    void createUserFolder(String username) throws StorageServerException;

    void uploadFile(FilesUploadDto filesUploadDto) throws StorageServerException;

    void uploadFolder(FilesUploadDto filesUploadDto) throws StorageServerException, ObjectAlreadyExistsException;

    void removeObject(StorageObjDto storageObjDto) throws StorageServerException;

    void createFolder(String username, String folderName) throws StorageServerException, ObjectAlreadyExistsException;

    void renameObject(StorageObjRenameDto storageObjectRenameDto)
            throws ObjectAlreadyExistsException, StorageServerException, StorageObjectNotFoundException;

    String getParentPath(String path);

    String getUserFolderName(String username);

    ByteArrayResource downloadFile(StorageObjDto storageObjDto) throws StorageServerException;

    List<BreadCrumb> getBreadCrumb(String path);

    List<StorageObject> getListOfObjects(String username, String objectName, boolean isRecursive)
            throws StorageServerException, StorageObjectNotFoundException;

    List<StorageObject> search(String username, String query)
            throws StorageServerException, StorageObjectNotFoundException;
}
