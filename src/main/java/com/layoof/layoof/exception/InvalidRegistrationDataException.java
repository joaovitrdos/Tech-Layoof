package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

/** Campo obrigatorio ausente no cadastro. */
public class InvalidRegistrationDataException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String TITLE = "Dados de cadastro invalidos";

    public static final String MISSING_NAME = "O name e obrigatorio";
    public static final String MISSING_EMAIL = "O email e obrigatorio";
    public static final String MISSING_PASSWORD = "A senha e obrigatoria";

    public InvalidRegistrationDataException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
