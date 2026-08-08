package com.layoof.layoof.exception;

public class InvalidRegistrationDataException extends RuntimeException {

    public static final String MISSING_NAME = "O nome e obrigatorio";
    public static final String MISSING_EMAIL = "O email e obrigatorio";
    public static final String MISSING_PASSWORD = "A senha e obrigatoria";

    public InvalidRegistrationDataException(String message) {
        super(message);
    }
}
