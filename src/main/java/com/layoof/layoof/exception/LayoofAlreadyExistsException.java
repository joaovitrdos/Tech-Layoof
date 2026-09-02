package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class LayoofAlreadyExistsException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String TITLE = "Demissao ja cadastrada";

    public LayoofAlreadyExistsException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public LayoofAlreadyExistsException(String detail, Throwable cause) {
        super(STATUS, TITLE, detail, cause);
    }
}
