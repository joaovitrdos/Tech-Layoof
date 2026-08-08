package com.layoof.layoof.exception;

public class EmailSendException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Falha ao enviar o email para: ";

    public EmailSendException(String recipient, Throwable cause) {
        super(DEFAULT_MESSAGE + recipient, cause);
    }
}
