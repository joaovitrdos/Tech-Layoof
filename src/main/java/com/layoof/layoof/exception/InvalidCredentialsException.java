package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;
    private static final String TITLE = "Credenciais invalidas";

    public InvalidCredentialsException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public InvalidCredentialsException(String detail, Throwable cause) {
        super(STATUS, TITLE, detail, cause);
    }
}
