package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyInUseException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String TITLE = "E-mail ja cadastrado";

    public EmailAlreadyInUseException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public EmailAlreadyInUseException(String detail, Throwable cause) {
        super(STATUS, TITLE, detail, cause);
    }
}
