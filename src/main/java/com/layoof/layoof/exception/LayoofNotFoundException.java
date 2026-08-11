package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

/** Demissao inexistente. */
public class LayoofNotFoundException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String TITLE = "Demissao nao encontrada";
    private static final String BY_ID = "Nenhuma demissao encontrada com o id: ";

    private LayoofNotFoundException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public static LayoofNotFoundException byId(UUID layoofId) {
        return new LayoofNotFoundException(BY_ID + layoofId);
    }
}
