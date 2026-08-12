package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class LayoofNotFoundException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String TITLE = "Demissao nao encontrada";

    public LayoofNotFoundException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
