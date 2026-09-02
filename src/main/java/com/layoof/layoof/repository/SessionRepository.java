package com.layoof.layoof.repository;

import com.layoof.layoof.entity.Session;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.enums.SessionRevocationReason;
import com.layoof.layoof.enums.SessionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    @EntityGraph(attributePaths = "user")
    Optional<Session> findByJti(UUID jti);

    @Modifying
    @Query("""
            UPDATE Session s
               SET s.status = :status, s.revokedReason = :reason, s.revokedAt = :now
             WHERE s.status = com.layoof.layoof.enums.SessionStatus.ACTIVE
               AND s.expiresAt < :now
            """)
    int closeIdleSessions(@Param("status") SessionStatus status,
                          @Param("reason") SessionRevocationReason reason,
                          @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Session s WHERE s.status <> com.layoof.layoof.enums.SessionStatus.ACTIVE "
            + "AND s.expiresAt < :moment")
    int deleteClosedBefore(@Param("moment") LocalDateTime moment);

    @Modifying
    @Query("DELETE FROM Session s WHERE s.user = :user")
    void deleteAllByUser(@Param("user") User user);
}
