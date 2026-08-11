package com.layoof.layoof.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traducao unica de excecao para resposta HTTP, no formato {@code ProblemDetail} (RFC 7807).
 *
 * <p>Eram treze handlers de quatro linhas quase identicas, um por excecao — e o preco disso
 * aparecia quando alguem esquecia de registrar a excecao nova: {@code EmailSendException} e
 * {@code InvalidCommentDataException} caiam no 500 generico, devolvendo "erro interno" para
 * erros que nao eram internos. Agora o status e o title moram na propria excecao
 * ({@link LayoofException}) e este arquivo nao precisa ser tocado quando surge uma regra nova.
 *
 * <p>Sobraram tres responsabilidades, e cada uma trata um tipo diferente de falha:
 *
 * <ul>
 *   <li><b>{@link LayoofException}</b> — erro previsto de negocio, com status proprio.</li>
 *   <li><b>{@code Exception}</b> — o que ninguem previu. Sempre 500, sempre com stack trace no
 *       log e <b>sem detalhe no corpo</b>: mensagem de excecao inesperada vaza nome de tabela,
 *       caminho de arquivo e versao de biblioteca.</li>
 *   <li><b>Erros do proprio Spring</b> (JSON malformado, parametro faltando, metodo errado) —
 *       padronizados em {@code handleExceptionInternal}, que e por onde todos eles passam.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String TIMESTAMP_FIELD = "timestamp";
    private static final String ERRORS_FIELD = "errors";

    private static final URI TYPE_INTERNAL = URI.create("/errors/internal");
    private static final URI TYPE_INVALID_REQUEST = URI.create("/errors/invalid-request");

    private static final String TITLE_INTERNAL = "Erro interno";
    private static final String TITLE_INVALID_REQUEST = "Requisicao invalida";
    private static final String DETAIL_INTERNAL = "Erro inesperado ao processar a requisicao";
    private static final String DETAIL_INVALID_DATA = "Dados invalidos na requisicao";

    /**
     * Todo erro de negocio do projeto passa por aqui.
     *
     * <p>O nivel do log segue o dono do problema: 4xx e do cliente e vira WARN sem stack trace,
     * porque senha errada nao e incidente. 5xx e nosso e vira ERROR com a excecao inteira.
     */
    @ExceptionHandler(LayoofException.class)
    public ProblemDetail handleLayoofException(LayoofException ex) {
        if (ex.isClientError()) {
            log.warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        } else {
            log.error("{}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        }

        ProblemDetail problem = buildProblem(ex.getStatus(), ex.getTitle(), ex.getMessage());
        problem.setType(ex.getType());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Erro nao tratado", ex);

        ProblemDetail problem = buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, TITLE_INTERNAL, DETAIL_INTERNAL);
        problem.setType(TYPE_INTERNAL);
        return problem;
    }

    /** Bean Validation: devolve o mapa campo/mensagem, que e o que o formulario precisa. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problem = buildProblem(
                HttpStatus.BAD_REQUEST, TITLE_INVALID_REQUEST, DETAIL_INVALID_DATA);
        problem.setType(TYPE_INVALID_REQUEST);
        problem.setProperty(ERRORS_FIELD, errors);

        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Funil de todas as excecoes que o proprio Spring levanta — JSON malformado, parametro
     * ausente, metodo nao suportado. Sem isto, essas respostas sairiam num formato e as nossas
     * em outro, e o front precisaria saber ler os dois.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             @Nullable Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {

        if (body instanceof ProblemDetail problem && problem.getProperties() == null) {
            problem.setProperty(TIMESTAMP_FIELD, LocalDateTime.now());
        }
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private ProblemDetail buildProblem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty(TIMESTAMP_FIELD, LocalDateTime.now());
        return problem;
    }
}
