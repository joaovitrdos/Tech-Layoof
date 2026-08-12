package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class InvalidRegistrationDataException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String TITLE = "Dados de cadastro invalidos";

    public InvalidRegistrationDataException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
