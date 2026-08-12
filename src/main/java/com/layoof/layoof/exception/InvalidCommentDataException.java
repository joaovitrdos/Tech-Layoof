package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class InvalidCommentDataException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String TITLE = "Dados do comentario invalidos";

    public InvalidCommentDataException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
