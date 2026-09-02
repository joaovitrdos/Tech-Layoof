package com.layoof.layoof.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "layoof.security.rate-limit")
public record RateLimitProperties(boolean enabled,
                                  boolean trustProxy,
                                  Policy auth,
                                  Policy standard) {

    private static final Policy DEFAULT_AUTH = new Policy(10, Duration.ofMinutes(1));
    private static final Policy DEFAULT_STANDARD = new Policy(120, Duration.ofMinutes(1));

    public RateLimitProperties {
        auth = auth == null ? DEFAULT_AUTH : auth;
        standard = standard == null ? DEFAULT_STANDARD : standard;
    }

    public record Policy(int requests, Duration window) {

        public Policy {
            window = window == null ? Duration.ofMinutes(1) : window;
        }
    }
}
