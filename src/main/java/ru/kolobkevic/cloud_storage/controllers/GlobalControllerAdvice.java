package ru.kolobkevic.cloud_storage.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.kolobkevic.cloud_storage.exceptions.ObjectAlreadyExistsException;
import ru.kolobkevic.cloud_storage.exceptions.StorageObjectNotFoundException;
import ru.kolobkevic.cloud_storage.exceptions.StorageServerException;

@ControllerAdvice
public class GlobalControllerAdvice {
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ObjectAlreadyExistsException.class)
    public String objectAlreadyExistsException() {
        return "/errors/error409";
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(StorageServerException.class)
    public String storageServerException() {
        return "/errors/error500";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(StorageObjectNotFoundException.class)
    public String storageObjectNotFoundException() {
        return "/errors/error404";
    }
}
