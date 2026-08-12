package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class GoogleAccountWithoutPasswordException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String TITLE = "Conta do Google";

    public GoogleAccountWithoutPasswordException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
