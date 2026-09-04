package com.layoof.layoof.entity;

import com.layoof.layoof.enums.NotificationFrequency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_notification_config", indexes = {
        @Index(name = "idx_notification_config_user", columnList = "user_id"),
        @Index(name = "idx_notification_config_frequency", columnList = "frequency")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final NotificationFrequency DEFAULT_FREQUENCY = NotificationFrequency.DAILY;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_config_id")
    private UUID notificationConfigId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 20)
    private NotificationFrequency frequency = DEFAULT_FREQUENCY;

    @Column(name = "last_sent_at")
    private LocalDateTime lastSentAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean sendsAt(LocalDateTime moment) {
        return frequency.sendsAt(moment);
    }
}
