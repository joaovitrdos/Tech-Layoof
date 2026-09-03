package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class FileStorageException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.SERVICE_UNAVAILABLE;
    private static final String TITLE = "Armazenamento indisponivel";

    public FileStorageException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public FileStorageException(String detail, Throwable cause) {
        super(STATUS, TITLE, detail, cause);
    }
}
