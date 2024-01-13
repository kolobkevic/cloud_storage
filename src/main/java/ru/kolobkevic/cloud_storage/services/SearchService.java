package ru.kolobkevic.cloud_storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.kolobkevic.cloud_storage.exceptions.StorageServerException;
import ru.kolobkevic.cloud_storage.models.StorageObject;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final StorageService storageService;

    public List<StorageObject> search(String username, String query) throws StorageServerException {
        var objects = storageService.getListOfObjects(username, "", true);
        List<StorageObject> searchObjects = objects.stream()
                .filter(obj -> obj.getObjectName().toLowerCase().contains(query.toLowerCase()))
                .toList();
        log.info("Search results: " + searchObjects);
        return searchObjects;
    }
}
