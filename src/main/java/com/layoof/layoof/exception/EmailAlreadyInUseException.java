package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

/** Cadastro com e-mail que ja existe. Conflito de estado, nao dado invalido: 409, nao 400. */
public class EmailAlreadyInUseException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String TITLE = "E-mail ja cadastrado";
    private static final String DETAIL = "Ja existe um usuario cadastrado com o email: ";

    public EmailAlreadyInUseException(String email) {
        super(STATUS, TITLE, DETAIL + email);
    }

    /**
     * Usado quando o {@code unique} do banco e quem detecta a colisao, depois da checagem previa.
     * A causa e preservada: e ela que diz qual constraint estourou.
     */
    public EmailAlreadyInUseException(String email, Throwable cause) {
        super(STATUS, TITLE, DETAIL + email, cause);
    }
}
