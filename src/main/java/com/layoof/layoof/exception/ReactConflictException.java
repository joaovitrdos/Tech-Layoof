package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class ReactConflictException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String TITLE = "Reacao em conflito";

    public ReactConflictException(String detail, Throwable cause) {
        super(STATUS, TITLE, detail, cause);
    }
}
