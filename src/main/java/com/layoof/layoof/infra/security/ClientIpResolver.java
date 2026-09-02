package com.layoof.layoof.infra.security;

import com.layoof.layoof.infra.config.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientIpResolver {

    private static final String FORWARDED_FOR = "X-Forwarded-For";
    private static final String UNKNOWN = "unknown";
    private static final int MAX_LENGTH = 64;

    private final RateLimitProperties properties;

    public String resolve(HttpServletRequest request) {
        if (properties.trustProxy()) {
            String forwarded = firstForwardedAddress(request.getHeader(FORWARDED_FOR));
            if (forwarded != null) {
                return forwarded;
            }
        }
        String remote = request.getRemoteAddr();
        return remote == null || remote.isBlank() ? UNKNOWN : remote;
    }

    private String firstForwardedAddress(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        String first = header.split(",")[0].strip();
        if (first.isBlank() || first.length() > MAX_LENGTH) {
            return null;
        }
        return first;
    }
}
