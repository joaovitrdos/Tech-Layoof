package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

/**
 * Tentativa de apagar comentario de outra pessoa.
 *
 * <p><b>403 e nao 404.</b> Ha uma escola que devolve 404 aqui para nao revelar que o recurso
 * existe. Nao se aplica: o comentario e conteudo publico, ja visivel na listagem, entao esconder
 * a existencia nao protege nada — so faz o front tratar "nao existe" e "nao e seu" do mesmo jeito
 * e mostrar a mensagem errada ao usuario.
 */
public class CommentNotOwnedException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;
    private static final String TITLE = "Comentario de outro usuario";

    public static final String NOT_THE_AUTHOR = "Voce so pode apagar os seus proprios comentarios";

    public CommentNotOwnedException() {
        super(STATUS, TITLE, NOT_THE_AUTHOR);
    }
}
