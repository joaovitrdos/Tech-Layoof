package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class ReactNotFoundException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String TITLE = "Reacao nao encontrada";

    public ReactNotFoundException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
