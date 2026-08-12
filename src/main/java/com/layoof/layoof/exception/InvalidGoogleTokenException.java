package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class InvalidGoogleTokenException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;
    private static final String TITLE = "Token do Google invalido";

    public InvalidGoogleTokenException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public InvalidGoogleTokenException(String detail, Throwable cause) {
        super(STATUS, TITLE, detail, cause);
    }
}
