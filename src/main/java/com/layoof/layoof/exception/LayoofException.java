package com.layoof.layoof.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.net.URI;
import java.util.Locale;

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

    public URI getType() {
        String name = getClass().getSimpleName();
        String base = name.endsWith(SUFFIX) ? name.substring(0, name.length() - SUFFIX.length()) : name;

        String slug = base.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);

        return URI.create(TYPE_PREFIX + slug);
    }

    public boolean isClientError() {
        return status.is4xxClientError();
    }
}
