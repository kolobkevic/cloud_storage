package ru.kolobkevic.cloud_storage.exceptions;

public class StorageServerException extends RuntimeException{
    public StorageServerException(String message) {
        super(message);
    }
}
