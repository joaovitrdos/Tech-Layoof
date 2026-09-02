package com.layoof.layoof.entity;

import com.layoof.layoof.enums.SourceType;
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
@Table(name = "TB_SOURCE")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Source implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "source_id", unique = true)
    private UUID sourceId;

    @Column(name = "name", columnDefinition = "TEXT", nullable = false)
    private String name;

    @Column(name = "feed_url", columnDefinition = "TEXT", nullable = false, unique = true)
    private String feedUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 16, nullable = false)
    private SourceType type;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "language", columnDefinition = "TEXT")
    private String language;

    @Column(name = "region", columnDefinition = "TEXT")
    private String region;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "last_fetched_at")
    private LocalDateTime lastFetchedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at")
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
}
