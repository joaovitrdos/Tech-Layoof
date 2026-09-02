package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class AiUnavailableException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.SERVICE_UNAVAILABLE;
    private static final String TITLE = "Consulta por IA indisponivel";

    public AiUnavailableException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public AiUnavailableException(String detail, Throwable cause) {
        super(STATUS, TITLE, detail, cause);
    }
}
