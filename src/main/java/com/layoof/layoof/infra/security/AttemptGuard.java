package com.layoof.layoof.infra.security;

import com.layoof.layoof.exception.TooManyAttemptsException;
import com.layoof.layoof.infra.config.LayoofSecurityProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AttemptGuard {

    private static final Logger log = LoggerFactory.getLogger(AttemptGuard.class);

    private static final String LOGIN_PREFIX = "rl:login:";
    private static final String RECOVERY_PREFIX = "rl:recovery:";
    private static final String UNKNOWN_ADDRESS = "unknown";

    private static final String LOGIN_BLOCKED =
            "Muitas tentativas de acesso para esta conta. Aguarde alguns minutos";
    private static final String RECOVERY_BLOCKED =
            "Muitos pedidos de recuperacao para este e-mail. Aguarde alguns minutos";

    private final ProxyManager<String> rateLimitProxyManager;
    private final LayoofSecurityProperties.RateLimit rateLimit;

    public AttemptGuard(ProxyManager<String> rateLimitProxyManager,
                        LayoofSecurityProperties securityProperties) {

        this.rateLimitProxyManager = rateLimitProxyManager;
        this.rateLimit = securityProperties.rateLimit();
    }

    public void checkLogin(String email) {
        if (exhausted(loginKey(email), rateLimit.loginAttempt())) {
            throw new TooManyAttemptsException(LOGIN_BLOCKED);
        }
    }

    public void recordLoginFailure(String email) {
        consume(loginKey(email), rateLimit.loginAttempt());
    }

    public void clearLogin(String email) {
        if (!rateLimit.enabled()) {
            return;
        }
        try {
            rateLimitProxyManager.removeProxy(loginKey(email));
        } catch (RuntimeException ex) {
            log.warn("Nao foi possivel limpar as tentativas de login: {}", ex.getMessage());
        }
    }

    public void checkRecovery(String email) {
        if (!consume(RECOVERY_PREFIX + email, rateLimit.recovery())) {
            throw new TooManyAttemptsException(RECOVERY_BLOCKED);
        }
    }

    private boolean exhausted(String key, LayoofSecurityProperties.Limit limit) {
        if (!rateLimit.enabled()) {
            return false;
        }
        try {
            return bucket(key, limit).getAvailableTokens() <= 0;
        } catch (RuntimeException ex) {
            log.warn("Contador de tentativas indisponivel, tentativa liberada: {}", ex.getMessage());
            return false;
        }
    }

    private boolean consume(String key, LayoofSecurityProperties.Limit limit) {
        if (!rateLimit.enabled()) {
            return true;
        }
        try {
            return bucket(key, limit).tryConsume(1);
        } catch (RuntimeException ex) {
            log.warn("Contador de tentativas indisponivel, tentativa liberada: {}", ex.getMessage());
            return true;
        }
    }

    private io.github.bucket4j.Bucket bucket(String key, LayoofSecurityProperties.Limit limit) {
        return rateLimitProxyManager.getProxy(key, () -> configurationOf(limit));
    }

    private BucketConfiguration configurationOf(LayoofSecurityProperties.Limit limit) {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit.capacity())
                        .refillGreedy(limit.capacity(), limit.window())
                        .build())
                .build();
    }

    private String loginKey(String email) {
        return LOGIN_PREFIX + email + ":" + clientAddress();
    }

    private String clientAddress() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRemoteAddr();
        }
        return UNKNOWN_ADDRESS;
    }
}
