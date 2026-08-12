package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class CommentWindowExpiredException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;
    private static final String TITLE = "Prazo encerrado";

    public CommentWindowExpiredException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
