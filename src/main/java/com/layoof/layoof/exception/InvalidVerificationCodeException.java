package com.layoof.layoof.exception;

public class InvalidVerificationCodeException extends RuntimeException {

    public static final String INVALID_CODE = "Codigo de verificacao invalido";
    public static final String EXPIRED_CODE = "Codigo de verificacao expirado";

    public InvalidVerificationCodeException(String message) {
        super(message);
    }
}
