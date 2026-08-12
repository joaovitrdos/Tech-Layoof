package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class IngestionInProgressException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String TITLE = "Coleta em andamento";

    public IngestionInProgressException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
