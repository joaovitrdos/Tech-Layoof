package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class UnauthenticatedException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;
    private static final String TITLE = "Nao autenticado";

    public UnauthenticatedException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
