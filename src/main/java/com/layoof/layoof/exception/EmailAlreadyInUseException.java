package com.layoof.layoof.exception;

public class EmailAlreadyInUseException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Ja existe um usuario cadastrado com o email: ";

    public EmailAlreadyInUseException(String email) {
        super(DEFAULT_MESSAGE + email);
    }

    public EmailAlreadyInUseException(String email, Throwable cause) {
        super(DEFAULT_MESSAGE + email, cause);
    }
}
