package com.layoof.layoof.entity;

import com.layoof.layoof.enums.SessionRevocationReason;
import com.layoof.layoof.enums.SessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TB_SESSION", indexes = {
        @Index(name = "idx_session_jti", columnList = "jti", unique = true),
        @Index(name = "idx_session_user_status", columnList = "user_id, status"),
        @Index(name = "idx_session_status_expires", columnList = "status, expires_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "jti", nullable = false, unique = true)
    private UUID jti;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private SessionStatus status;

    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revoked_reason", length = 16)
    private SessionRevocationReason revokedReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = SessionStatus.ACTIVE;
        }
    }

    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }

    public boolean isIdleFor(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    public boolean isUsableAt(LocalDateTime now) {
        return isActive() && !isIdleFor(now);
    }

    public void touch(LocalDateTime now, Duration idleTtl) {
        lastActivityAt = now;
        expiresAt = now.plus(idleTtl);
    }

    public boolean needsTouch(LocalDateTime now, Duration tolerance) {
        return lastActivityAt.plus(tolerance).isBefore(now);
    }

    public void expire(LocalDateTime now) {
        close(SessionStatus.EXPIRED, SessionRevocationReason.INACTIVITY, now);
    }

    public void revoke(LocalDateTime now) {
        close(SessionStatus.REVOKED, SessionRevocationReason.LOGOUT, now);
    }

    private void close(SessionStatus newStatus, SessionRevocationReason reason, LocalDateTime now) {
        if (!isActive()) {
            return;
        }
        status = newStatus;
        revokedReason = reason;
        revokedAt = now;
    }
}
