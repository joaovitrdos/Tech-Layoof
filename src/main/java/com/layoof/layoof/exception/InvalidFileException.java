package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class InvalidFileException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String TITLE = "Arquivo invalido";

    public InvalidFileException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public InvalidFileException(String detail, Throwable cause) {
        super(STATUS, TITLE, detail, cause);
    }
}
