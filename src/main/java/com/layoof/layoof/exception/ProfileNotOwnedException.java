package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

public class ProfileNotOwnedException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;
    private static final String TITLE = "Perfil de outro usuario";

    public ProfileNotOwnedException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
