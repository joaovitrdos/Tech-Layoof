package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class LayoofNotOwnedException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;
    private static final String TITLE = "Demissao de outro usuario";

    public LayoofNotOwnedException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
