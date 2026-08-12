package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.net.URI;
import java.util.Locale;

public final class ProblemTypes {

    public static final URI BLANK = URI.create("about:blank");

    private static final String PREFIX = "/errors/";

    private ProblemTypes() {
    }

    public static URI typeFor(HttpStatusCode status) {
        HttpStatus resolved = HttpStatus.resolve(status.value());

        String slug = resolved == null
                ? "http-" + status.value()
                : resolved.name().toLowerCase(Locale.ROOT).replace('_', '-');

        return URI.create(PREFIX + slug);
    }

    public static String titleFor(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Requisicao invalida";
            case 401 -> "Nao autenticado";
            case 403 -> "Acesso negado";
            case 404 -> "Recurso nao encontrado";
            case 405 -> "Metodo nao permitido";
            case 406 -> "Formato nao aceito";
            case 409 -> "Conflito";
            case 415 -> "Formato nao suportado";
            case 429 -> "Requisicoes em excesso";
            case 500 -> "Erro interno";
            default -> status.is4xxClientError() ? "Requisicao invalida" : "Erro interno";
        };
    }
}
