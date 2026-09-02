package com.layoof.layoof.service;

import com.layoof.layoof.entity.Session;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.enums.SessionRevocationReason;
import com.layoof.layoof.enums.SessionStatus;
import com.layoof.layoof.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    static final Duration IDLE_TTL = Duration.ofMinutes(30);
    static final Duration ACTIVITY_TOLERANCE = Duration.ofSeconds(60);

    private static final Duration RETENTION = Duration.ofDays(30);

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;

    @Transactional
    public UUID open(User user) {
        LocalDateTime now = LocalDateTime.now();
        UUID jti = UUID.randomUUID();

        sessionRepository.save(Session.builder()
                .jti(jti)
                .user(user)
                .status(SessionStatus.ACTIVE)
                .lastActivityAt(now)
                .expiresAt(now.plus(IDLE_TTL))
                .build());

        return jti;
    }

    @Transactional
    public Optional<User> authenticate(UUID jti) {
        Session session = sessionRepository.findByJti(jti).orElse(null);

        if (session == null || !session.isActive()) {
            return Optional.empty();
        }

        LocalDateTime now = LocalDateTime.now();

        if (session.isIdleFor(now)) {
            session.expire(now);
            log.debug("Sessao {} expirada por inatividade", session.getSessionId());
            return Optional.empty();
        }

        if (session.needsTouch(now, ACTIVITY_TOLERANCE)) {
            session.touch(now, IDLE_TTL);
        }

        return Optional.of(session.getUser());
    }

    @Transactional
    public void revoke(UUID jti) {
        sessionRepository.findByJti(jti)
                .ifPresent(session -> session.revoke(LocalDateTime.now()));
    }

    @Transactional
    @Scheduled(fixedDelay = 300_000)
    public void closeIdleSessions() {
        int closed = sessionRepository.closeIdleSessions(
                SessionStatus.EXPIRED, SessionRevocationReason.INACTIVITY, LocalDateTime.now());

        if (closed > 0) {
            log.info("Encerradas {} sessoes por inatividade", closed);
        }
    }

    @Transactional
    @Scheduled(cron = "0 30 3 * * *")
    public void deleteOldSessions() {
        int removed = sessionRepository.deleteClosedBefore(LocalDateTime.now().minus(RETENTION));

        if (removed > 0) {
            log.info("Removidas {} sessoes encerradas ha mais de {} dias", removed, RETENTION.toDays());
        }
    }
}
