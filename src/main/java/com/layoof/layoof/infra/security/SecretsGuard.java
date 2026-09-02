package com.layoof.layoof.infra.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class SecretsGuard {

    private static final int MIN_LENGTH = 32;
    private static final int MIN_DISTINCT_CHARS = 12;

    private static final Set<String> PLACEHOLDERS = Set.of(
            "changeme", "change-me", "secret", "mysecret", "my-secret", "my-secret-key",
            "password", "senha", "token", "apikey", "api-key", "todo", "test", "teste",
            "dev", "local", "example", "exemplo", "123456", "12345678");

    private final String jwtSecret;

    public SecretsGuard(@Value("${api.security.token.secret:}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @PostConstruct
    void validate() {
        require(jwtSecret, "LAYOOF_JWT_SECRET");
    }

    private void require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(missing(name));
        }

        String clean = value.strip();

        if (clean.length() < MIN_LENGTH) {
            throw new IllegalStateException(weak(name,
                    "tem %d caracteres e o minimo e %d".formatted(clean.length(), MIN_LENGTH)));
        }
        if (PLACEHOLDERS.contains(clean.toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException(weak(name, "e um valor de exemplo"));
        }
        if (distinctChars(clean) < MIN_DISTINCT_CHARS) {
            throw new IllegalStateException(weak(name,
                    "tem pouca variacao de caracteres e e previsivel demais"));
        }
    }

    private long distinctChars(String value) {
        return value.chars().distinct().count();
    }

    private String missing(String name) {
        return "%s nao esta definido. Gere um segredo com 'openssl rand -base64 48' e coloque no .env"
                .formatted(name);
    }

    private String weak(String name, String reason) {
        return "%s %s. Gere um segredo novo com 'openssl rand -base64 48' e coloque no .env"
                .formatted(name, reason);
    }
}
