package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

/**
 * Operacao que exige usuario autenticado chegou sem principal.
 *
 * <p>Com a cadeia de seguranca correta isto nao acontece: {@code anyRequest().authenticated()} ja
 * barra antes. Existe porque a alternativa e pior — sem a checagem, um principal nulo viraria
 * {@code NullPointerException} la dentro, com 500 no cliente e stack trace no log em vez de um
 * 401 claro.
 *
 * <p>E a guarda que faz o service falhar fechado se um dia alguem liberar a rota por engano: a
 * autorizacao deixa de depender exclusivamente da configuracao do Spring Security.
 */
public class UnauthenticatedException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;
    private static final String TITLE = "Nao autenticado";

    public static final String LOGIN_REQUIRED = "E preciso estar autenticado para executar esta operacao";

    public UnauthenticatedException() {
        super(STATUS, TITLE, LOGIN_REQUIRED);
    }
}
