package ru.kolobkevic.cloud_storage.exceptions;

public class StorageObjectNotFoundException extends Exception{
    public StorageObjectNotFoundException(String message) {
        super(message);
    }
}
