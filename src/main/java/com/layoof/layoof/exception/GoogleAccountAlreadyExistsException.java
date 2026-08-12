package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class GoogleAccountAlreadyExistsException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String TITLE = "Conta vinculada ao Google";

    public GoogleAccountAlreadyExistsException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public GoogleAccountAlreadyExistsException(String detail, Throwable cause) {
        super(STATUS, TITLE, detail, cause);
    }
}
