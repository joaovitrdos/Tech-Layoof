package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Usuario inexistente.
 *
 * <p>Os construtores recebem o <b>identificador</b>, nao a mensagem pronta. E a convencao do
 * projeto (ver {@link EmailAlreadyInUseException}) e ela importa: com a mensagem montada no
 * chamador, o mesmo erro sai escrito de tres jeitos diferentes conforme quem lanca. As fabricas
 * estaticas nomeiam o criterio da busca, que e a informacao util no log.
 */
public class UserNotFoundException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String TITLE = "Usuario nao encontrado";

    private static final String BY_EMAIL = "Nenhum usuario encontrado com o email: ";
    private static final String BY_ID = "Nenhum usuario encontrado com o id: ";

    private UserNotFoundException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException(BY_EMAIL + email);
    }

    public static UserNotFoundException byId(UUID userId) {
        return new UserNotFoundException(BY_ID + userId);
    }
}
