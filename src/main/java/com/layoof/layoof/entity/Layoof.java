package com.layoof.layoof.entity;

import com.layoof.layoof.enums.LayoofConfidence;
import com.layoof.layoof.enums.LayoofStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "TB_LAYOOF", indexes = {
        @Index(name = "idx_layoof_title_fingerprint", columnList = "title_fingerprint"),
        @Index(name = "idx_layoof_company", columnList = "company"),
        @Index(name = "idx_layoof_status_published_at", columnList = "status, published_at"),
        @Index(name = "idx_layoof_author", columnList = "author_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Layoof implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "layoof_id", unique = true)
    private UUID layoofId;

    @Column(name = "company", columnDefinition = "TEXT")
    private String company;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "numbers_of_cuts")
    private Integer numbersOfCuts;

    @Column(name = "city", columnDefinition = "TEXT")
    private String city;

    @Column(name = "country", columnDefinition = "TEXT")
    private String country;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "source_url", columnDefinition = "TEXT", unique = true, nullable = false)
    private String sourceUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private LayoofStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "confidence", length = 8)
    private LayoofConfidence confidence;

    @Column(name = "title_fingerprint", length = 64)
    private String titleFingerprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @OneToMany(mappedBy = "layoof", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = this.createdAt;
        if (status == null) {
            status = LayoofStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isAuthoredBy(User user) {
        return user != null && author != null && author.getUserId().equals(user.getUserId());
    }
}
