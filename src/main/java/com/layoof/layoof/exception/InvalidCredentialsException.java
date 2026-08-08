package com.layoof.layoof.exception;

public class InvalidCredentialsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Email ou senha invalidos";


    public InvalidCredentialsException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
