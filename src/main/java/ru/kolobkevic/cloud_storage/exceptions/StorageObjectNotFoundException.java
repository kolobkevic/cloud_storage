package ru.kolobkevic.cloud_storage.exceptions;

public class StorageObjectNotFoundException extends RuntimeException{
    public StorageObjectNotFoundException(String message) {
        super(message);
    }
}
