package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

/**
 * Ja ha um ciclo de coleta rodando.
 *
 * <p>Recusar e melhor do que enfileirar: dois agentes varrendo as mesmas fontes ao mesmo tempo
 * pagam token em dobro pelo mesmo trabalho, e quem chamou prefere saber na hora a ficar com a
 * requisicao pendurada por minutos.
 */
public class IngestionInProgressException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String TITLE = "Coleta em andamento";

    public static final String ALREADY_RUNNING =
            "Ja existe um ciclo de coleta em andamento. Tente novamente em instantes.";

    public IngestionInProgressException() {
        super(STATUS, TITLE, ALREADY_RUNNING);
    }
}
