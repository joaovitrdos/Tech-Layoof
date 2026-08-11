package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

/** Comentario inexistente. */
public class CommentNotFoundException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String TITLE = "Comentario nao encontrado";
    private static final String BY_ID = "Nenhum comentario encontrado com o id: ";

    private CommentNotFoundException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public static CommentNotFoundException byId(UUID commentId) {
        return new CommentNotFoundException(BY_ID + commentId);
    }
}
