package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class TooManyAttemptsException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.TOO_MANY_REQUESTS;
    private static final String TITLE = "Tentativas em excesso";

    public TooManyAttemptsException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
