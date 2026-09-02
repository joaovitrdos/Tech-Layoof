package com.layoof.layoof.repository;

import com.layoof.layoof.entity.React;
import com.layoof.layoof.enums.ReactType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReactRepository extends JpaRepository<React, UUID> {

    Optional<React> findByCommentCommentIdAndAuthorUserId(UUID commentId, UUID userId);

    List<React> findByCommentCommentIdOrderByCreatedAtDesc(UUID commentId);

    @Query("""
            select r.type as type, count(r) as total
            from React r
            where r.comment.commentId = :commentId
            group by r.type
            """)
    List<ReactCountProjection> countByTypeForComment(@Param("commentId") UUID commentId);

    interface ReactCountProjection {

        ReactType getType();

        long getTotal();
    }
}
