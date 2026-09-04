package com.layoof.layoof.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "layoof.security")
public record LayoofSecurityProperties(List<String> allowedOrigins,
                                       boolean docsEnabled,
                                       RateLimit rateLimit) {

    private static final List<String> DEFAULT_ORIGINS = List.of("http://localhost:5173");

    public LayoofSecurityProperties {
        allowedOrigins = allowedOrigins == null || allowedOrigins.isEmpty()
                ? DEFAULT_ORIGINS
                : List.copyOf(allowedOrigins);
    }

    public record RateLimit(boolean enabled,
                            Limit auth,
                            Limit ai,
                            Limit user,
                            Limit anonymous,
                            Limit loginAttempt,
                            Limit recovery) {
    }

    public record Limit(long capacity, Duration window) {
    }
}
