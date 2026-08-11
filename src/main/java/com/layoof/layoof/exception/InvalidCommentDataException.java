package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

/**
 * Comentario com dado invalido.
 *
 * <p>Antes nao tinha handler e caia no 500 generico: o cliente recebia "erro interno" para um
 * erro que era dele. Herdando de {@link LayoofException} o status vem junto da regra.
 */
public class InvalidCommentDataException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String TITLE = "Dados do comentario invalidos";

    public static final String MISSING_CONTENT = "O conteudo do comentario e obrigatorio";
    public static final String MISSING_AUTHOR = "O autor do comentario e obrigatorio";

    public InvalidCommentDataException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
