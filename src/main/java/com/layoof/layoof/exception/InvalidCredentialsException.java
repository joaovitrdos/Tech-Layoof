package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

/**
 * Credenciais que nao conferem.
 *
 * <p>A mensagem e deliberadamente vaga e igual para e-mail inexistente e senha errada: distinguir
 * os dois casos entrega ao atacante a lista de e-mails cadastrados.
 */
public class InvalidCredentialsException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;
    private static final String TITLE = "Credenciais invalidas";

    public static final String INVALID_LOGIN = "Email ou senha invalidos";

    public InvalidCredentialsException() {
        this(INVALID_LOGIN);
    }

    public InvalidCredentialsException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public InvalidCredentialsException(String detail, Throwable cause) {
        super(STATUS, TITLE, detail, cause);
    }
}
