package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

import java.time.Duration;

/**
 * A janela para editar ou apagar o comentario ja fechou.
 *
 * <p><b>403 e nao 409:</b> o pedido esta bem formado e o recurso esta no estado esperado — o que
 * falta e permissao, que existia e expirou. O cliente nao tem o que corrigir e reenviar, entao
 * "conflito" mandaria a mensagem errada.
 *
 * <p>Uma classe para os dois casos: mesmo status, ninguem captura um em separado, e o nome ja
 * documenta a regra. Os dois textos sao construidos a partir da {@link Duration} configurada, para
 * a mensagem nunca discordar do valor que esta valendo de fato.
 */
public class CommentWindowExpiredException extends LayoofException {

    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;
    private static final String TITLE = "Prazo encerrado";

    private static final String EDIT = "Comentarios so podem ser editados nos primeiros %d minutos apos a criacao";
    private static final String DELETE = "Comentarios so podem ser apagados nos primeiros %d minutos apos a criacao";

    private CommentWindowExpiredException(String detail) {
        super(STATUS, TITLE, detail);
    }

    public static CommentWindowExpiredException forEdit(Duration window) {
        return new CommentWindowExpiredException(EDIT.formatted(window.toMinutes()));
    }

    public static CommentWindowExpiredException forDelete(Duration window) {
        return new CommentWindowExpiredException(DELETE.formatted(window.toMinutes()));
    }
}
