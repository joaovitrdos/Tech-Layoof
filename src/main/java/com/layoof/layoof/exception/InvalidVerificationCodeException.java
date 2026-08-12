package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class InvalidVerificationCodeException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String TITLE = "Codigo de verificacao invalido";

    public InvalidVerificationCodeException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
