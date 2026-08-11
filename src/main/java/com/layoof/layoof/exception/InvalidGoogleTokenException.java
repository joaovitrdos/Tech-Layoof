package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

/**
 * Token do Google ausente, invalido ou incompleto.
 *
 * <p>Seis motivos, uma classe. As constantes existem porque nenhum dos seis casos muda o status,
 * ninguem captura um deles em separado e o name da classe ja documenta a regra — os tres testes
 * que justificariam classes proprias falham. Seis classes aqui seriam cerimonia.
 */
public class InvalidGoogleTokenException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;
    private static final String TITLE = "Token do Google invalido";

    public static final String MISSING_SUBJECT = "O token do Google nao contem o identificador do usuario";
    public static final String MISSING_EMAIL = "O token do Google nao contem um e-mail";
    public static final String EMAIL_NOT_VERIFIED = "O e-mail da conta Google nao foi verificado pelo Google";
    public static final String NOT_INFORMED = "Token do Google nao informado";
    public static final String INVALID_OR_EXPIRED = "Token do Google invalido ou expirado";
    public static final String NOT_VERIFIABLE = "Nao foi possivel validar o token do Google";

    public InvalidGoogleTokenException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public InvalidGoogleTokenException(String detail, Throwable cause) {
        super(STATUS, TITLE, detail, cause);
    }
}
