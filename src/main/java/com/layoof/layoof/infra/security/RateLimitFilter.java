package com.layoof.layoof.infra.security;

import com.layoof.layoof.exception.ProblemDetailWriter;
import com.layoof.layoof.infra.config.LayoofSecurityProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String TOO_MANY = "Muitas requisicoes. Tente novamente em alguns instantes";
    private static final String REMAINING_HEADER = "X-RateLimit-Remaining";
    private static final String AUTH_PREFIX = "/auth";
    private static final String AI_PATH = "/layoofs/ai";
    private static final String FILES_PREFIX = "/files";

    private final ProxyManager<String> rateLimitProxyManager;
    private final LayoofSecurityProperties.RateLimit rateLimit;
    private final TokenService tokenService;
    private final ProblemDetailWriter problemDetailWriter;

    public RateLimitFilter(ProxyManager<String> rateLimitProxyManager,
                           LayoofSecurityProperties securityProperties,
                           TokenService tokenService,
                           ProblemDetailWriter problemDetailWriter) {

        this.rateLimitProxyManager = rateLimitProxyManager;
        this.rateLimit = securityProperties.rateLimit();
        this.tokenService = tokenService;
        this.problemDetailWriter = problemDetailWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Target target = targetFor(request);
        ConsumptionProbe probe = tryConsume(target);

        if (probe == null) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!probe.isConsumed()) {
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(secondsUntilRefill(probe)));
            problemDetailWriter.write(request, response, HttpStatus.TOO_MANY_REQUESTS, TOO_MANY);
            return;
        }

        response.setHeader(REMAINING_HEADER, String.valueOf(probe.getRemainingTokens()));
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !rateLimit.enabled()
                || HttpMethod.OPTIONS.matches(request.getMethod())
                || request.getRequestURI().startsWith(FILES_PREFIX);
    }

    private Target targetFor(HttpServletRequest request) {
        String path = request.getRequestURI();
        String email = tokenService.emailFrom(request.getHeader(HttpHeaders.AUTHORIZATION));

        if (path.startsWith(AUTH_PREFIX)) {
            return new Target("auth:" + request.getRemoteAddr(), rateLimit.auth());
        }
        if (email == null) {
            return new Target("ip:" + request.getRemoteAddr(), rateLimit.anonymous());
        }
        if (path.startsWith(AI_PATH)) {
            return new Target("ai:" + email, rateLimit.ai());
        }
        return new Target("user:" + email, rateLimit.user());
    }

    private ConsumptionProbe tryConsume(Target target) {
        try {
            return rateLimitProxyManager
                    .getProxy("rl:" + target.key(), () -> configurationOf(target.limit()))
                    .tryConsumeAndReturnRemaining(1);
        } catch (RuntimeException ex) {
            logger.warn("Rate limit indisponivel, requisicao liberada: " + ex.getMessage());
            return null;
        }
    }

    private BucketConfiguration configurationOf(LayoofSecurityProperties.Limit limit) {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit.capacity())
                        .refillGreedy(limit.capacity(), limit.window())
                        .build())
                .build();
    }

    private long secondsUntilRefill(ConsumptionProbe probe) {
        return TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;
    }

    private record Target(String key, LayoofSecurityProperties.Limit limit) {
    }
}
