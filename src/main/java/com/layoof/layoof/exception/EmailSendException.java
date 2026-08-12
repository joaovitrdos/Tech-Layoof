package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class EmailSendException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.BAD_GATEWAY;
    private static final String TITLE = "Falha no envio de e-mail";

    public EmailSendException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public EmailSendException(String detail, Throwable cause) {
        super(STATUS, TITLE, detail, cause);
    }
}
