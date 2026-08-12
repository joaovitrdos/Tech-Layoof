package com.layoof.layoof.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private static final String DETAIL_MALFORMED_BODY = "Corpo da requisicao ausente ou malformado";

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

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {

        String detail = describeUnreadableBody(ex);
        log.warn("Corpo da requisicao rejeitado: {}", detail);

        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST, TITLE_INVALID_REQUEST, detail);
        problem.setType(TYPE_INVALID_REQUEST);

        return ResponseEntity.badRequest().body(problem);
    }

    private String describeUnreadableBody(HttpMessageNotReadableException ex) {
        if (!(ex.getCause() instanceof InvalidFormatException invalidFormat)) {
            return DETAIL_MALFORMED_BODY;
        }

        String field = invalidFormat.getPath().stream()
                .map(JacksonException.Reference::getPropertyName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));

        if (field.isBlank()) {
            return DETAIL_MALFORMED_BODY;
        }

        Class<?> targetType = invalidFormat.getTargetType();
        if (targetType == null || !targetType.isEnum()) {
            return "Valor invalido para o campo '%s'".formatted(field);
        }

        return "Valor invalido para o campo '%s'. Valores aceitos: %s";
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             @Nullable Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {

        if (body instanceof ProblemDetail problem) {
            standardize(problem, statusCode);
        }
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private void standardize(ProblemDetail problem, HttpStatusCode status) {
        if (ProblemTypes.BLANK.equals(problem.getType())) {
            problem.setType(ProblemTypes.typeFor(status));
            problem.setTitle(ProblemTypes.titleFor(status));
        }

        Map<String, Object> properties = problem.getProperties();
        if (properties == null || !properties.containsKey(TIMESTAMP_FIELD)) {
            problem.setProperty(TIMESTAMP_FIELD, LocalDateTime.now());
        }
    }

    private ProblemDetail buildProblem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty(TIMESTAMP_FIELD, LocalDateTime.now());
        return problem;
    }
}
