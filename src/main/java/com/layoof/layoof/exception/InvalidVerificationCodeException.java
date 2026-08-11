package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

/**
 * Codigo de recuperacao invalido ou expirado.
 *
 * <p>Os dois casos compartilham status e title, e nenhum e capturado em separado — por isso sao
 * constantes numa classe so, e nao duas classes.
 */
public class InvalidVerificationCodeException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String TITLE = "Codigo de verificacao invalido";

    public static final String INVALID_CODE = "Codigo de verificacao invalido";
    public static final String EXPIRED_CODE = "Codigo de verificacao expirado";

    public InvalidVerificationCodeException(String detail) {
        super(STATUS, TITLE, detail);
    }
}
