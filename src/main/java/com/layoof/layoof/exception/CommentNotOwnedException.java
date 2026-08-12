package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class CommentNotOwnedException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;
    private static final String TITLE = "Comentario de outro usuario";

    public CommentNotOwnedException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
