package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

/**
 * Falha ao falar com o SMTP.
 *
 * <p>502 e nao 500: o defeito esta num sistema a montante, nao na nossa aplicacao. A distincao
 * importa em alerta e em painel — 500 significa "temos um bug", 502 significa "o provedor caiu".
 *
 * <p>Antes esta excecao nao tinha handler e caia no 500 generico, escondendo a causa de quem
 * chamava.
 */
public class EmailSendException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.BAD_GATEWAY;
    private static final String TITLE = "Falha no envio de e-mail";
    private static final String DETAIL = "Falha ao enviar o email para: ";

    public EmailSendException(String recipient, Throwable cause) {
        super(STATUS, TITLE, DETAIL + recipient, cause);
    }
}
