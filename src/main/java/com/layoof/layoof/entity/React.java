package com.layoof.layoof.entity;

import com.layoof.layoof.enums.ReactType;
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
@Table(
        name = "TB_REACT",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_react_comment_author",
                columnNames = {"comment_id", "author_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class React implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "react_id", unique = true)
    private UUID reactId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 16, nullable = false)
    private ReactType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

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

    public boolean isAuthoredBy(User user) {
        return user != null && author != null && author.getUserId().equals(user.getUserId());
    }

    public boolean hasType(ReactType other) {
        return type == other;
    }
}
