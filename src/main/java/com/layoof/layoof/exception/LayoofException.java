package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.net.URI;
import java.util.Locale;

/**
 * Raiz de toda excecao de negocio do Tech Layoof.
 *
 * <p><b>Por que herda de {@link RuntimeException} e nao de {@code Exception}:</b> o Spring so
 * faz rollback automatico em {@code RuntimeException} e {@code Error}. Uma checked exception
 * lancada de dentro de um metodo {@code @Transactional} <b>faz commit</b> — estado pela metade,
 * em silencio. Alem disso, numa API REST quem chama o service e um controller sem plano B: a
 * unica resposta honesta e um status code, entao obrigar {@code throws} sobe uma obrigacao que
 * ninguem pode cumprir.
 *
 * <p><b>Por que status e titulo moram aqui:</b> antes, cada excecao precisava de um
 * {@code @ExceptionHandler} proprio no advice, e esquecer de registrar um significava 500
 * generico no lugar de 404 — foi o que aconteceu com {@code EmailSendException}. Com o status
 * na propria excecao, quem cria a regra declara a resposta no mesmo lugar, e o
 * {@link GlobalExceptionHandler} nao precisa ser tocado.
 *
 * <p>Regra para novas excecoes: crie uma classe quando ela mapear para um <b>status diferente</b>,
 * quando alguem for <b>captura-la especificamente</b> (codigo ou teste) ou quando o <b>nome
 * documentar a regra</b>. Se nenhum dos tres valer, reaproveite uma existente com outra mensagem
 * — e por isso que {@link InvalidGoogleTokenException} tem constantes em vez de seis classes.
 *
 * <p>Erro de programacao (invariante quebrada, argumento que so chega errado se o codigo estiver
 * errado) <b>nao</b> entra nesta hierarquia: e {@code IllegalStateException} ou
 * {@code IllegalArgumentException}, e 500 e a resposta certa porque e bug.
 */
public abstract class LayoofException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String SUFFIX = "Exception";
    private static final String TYPE_PREFIX = "/errors/";

    private final HttpStatus status;
    private final String title;

    protected LayoofException(HttpStatus status, String title, String detail) {
        this(status, title, detail, null);
    }

    /**
     * Sempre prefira este construtor quando houver uma causa. Descartar a {@code cause} joga
     * fora exatamente a informacao de que se precisa as tres da manha.
     */
    protected LayoofException(HttpStatus status, String title, String detail, Throwable cause) {
        super(detail, cause);
        this.status = status;
        this.title = title;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Identificador estavel do erro, derivado do nome da classe:
     * {@code UserNotFoundException} vira {@code user-not-found}. Vai no campo {@code type} do
     * {@code ProblemDetail} (RFC 7807) e e o que o front pode usar para tratar um caso
     * especifico sem depender do texto da mensagem, que muda.
     */
    public URI getType() {
        String name = getClass().getSimpleName();
        String base = name.endsWith(SUFFIX) ? name.substring(0, name.length() - SUFFIX.length()) : name;

        String slug = base.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);

        return URI.create(TYPE_PREFIX + slug);
    }

    /** Erro do cliente e log de WARN; erro nosso e log de ERROR com stack trace. */
    public boolean isClientError() {
        return status.is4xxClientError();
    }
}
