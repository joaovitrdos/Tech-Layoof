package com.layoof.layoof.entity;

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
@Table(name = "TB_LAYOOF")
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

    @Column(name = "company")
    private String company;

    @Column(name = "title", length = 512)
    private String title;

    @Column(name = "numbers_of_cuts")
    private Integer numbersOfCuts;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(name = "source_url", length = 1024, unique = true, nullable = false)
    private String sourceUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
