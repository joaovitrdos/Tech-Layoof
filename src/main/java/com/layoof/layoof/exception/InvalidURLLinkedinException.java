package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class InvalidURLLinkedinException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String TITLE = "URL Linkedin verificacao invalido";

    public InvalidURLLinkedinException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
