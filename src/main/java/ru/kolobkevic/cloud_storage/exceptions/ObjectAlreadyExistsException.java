package ru.kolobkevic.cloud_storage.exceptions;

public class ObjectAlreadyExistsException extends Exception {
    public ObjectAlreadyExistsException(String message) {
        super(message);
    }
}
