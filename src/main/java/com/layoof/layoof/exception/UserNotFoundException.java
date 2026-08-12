package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String TITLE = "Usuario nao encontrado";

    public UserNotFoundException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
