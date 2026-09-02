package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class InvalidLayoofDataException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String TITLE = "Dados da demissao invalidos";

    public InvalidLayoofDataException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
