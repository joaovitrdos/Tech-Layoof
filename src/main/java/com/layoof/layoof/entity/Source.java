package com.layoof.layoof.entity;

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

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "feed_url", length = 1024, nullable = false, unique = true)
    private String feedUrl;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /** "pt" ou "en". A IA precisa cobrir os dois para nao perder corte local nem multinacional. */
    @Column(name = "language", length = 8)
    private String language;

    /** "BR", "US" ou "GLOBAL". */
    @Column(name = "region", length = 16)
    private String region;

    /** O que a IA precisa saber para decidir se vale consultar esta fonte. */
    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "last_fetched_at")
    private LocalDateTime lastFetchedAt;

    /** Ultimo erro de coleta. E a primeira coisa que o suporte olha quando uma fonte seca. */
    @Column(name = "last_error", length = 512)
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
