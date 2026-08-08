package com.layoof.layoof.exception;

public class InvalidGoogleTokenException extends RuntimeException {

    public static final String MISSING_SUBJECT = "O token do Google nao contem o identificador do usuario";
    public static final String MISSING_EMAIL = "O token do Google nao contem um e-mail";
    public static final String EMAIL_NOT_VERIFIED = "O e-mail da conta Google nao foi verificado pelo Google";
    public static final String NOT_INFORMED = "Token do Google nao informado";
    public static final String INVALID_OR_EXPIRED = "Token do Google invalido ou expirado";
    public static final String NOT_VERIFIABLE = "Nao foi possivel validar o token do Google";

    public InvalidGoogleTokenException(String message) {
        super(message);
    }

    public InvalidGoogleTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
