package com.layoof.layoof.infra.security;

import com.layoof.layoof.exception.ProblemDetailWriter;
import com.layoof.layoof.infra.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String PREFLIGHT = "OPTIONS";
    private static final String AUTH_PATH = "/auth/";
    private static final String DENIED = "Muitas requisicoes em pouco tempo. Tente novamente daqui a instantes";

    private final RateLimiter rateLimiter;
    private final RateLimitProperties properties;
    private final ClientIpResolver clientIpResolver;
    private final ProblemDetailWriter problemDetailWriter;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        RateLimitProperties.Policy policy = policyFor(path);
        String key = bucketFor(path) + "|" + clientIpResolver.resolve(request);

        RateLimiter.Decision decision = rateLimiter.check(key, policy.requests(), policy.window());
        if (!decision.allowed()) {
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(decision.retryAfterSeconds()));
            problemDetailWriter.write(request, response, HttpStatus.TOO_MANY_REQUESTS, DENIED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.enabled() || PREFLIGHT.equals(request.getMethod());
    }

    private RateLimitProperties.Policy policyFor(String path) {
        if (path.startsWith(AUTH_PATH)) {
            return properties.auth();
        }
        return properties.standard();
    }

    private String bucketFor(String path) {
        if (path.startsWith(AUTH_PATH)) {
            return AUTH_PATH;
        }
        return "/";
    }
}
