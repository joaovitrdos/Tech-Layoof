package com.layoof.layoof.repository;

import com.layoof.layoof.entity.React;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReactRepository extends JpaRepository<React, UUID> {

    List<React> findByAuthorUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<React> findByCommentCommentIdAndAuthorUserId(UUID commentId, UUID userId);

    Optional<React> findByLayoofLayoofIdAndAuthorUserId(UUID layoofId, UUID userId);

    @Query("""
            select count(case when r.type = com.layoof.layoof.enums.ReactType.LIKE then 1 end) as likes,
                   count(case when r.type = com.layoof.layoof.enums.ReactType.DISLIKE then 1 end) as dislikes
            from React r
            where r.comment.commentId = :commentId
            """)
    ReactCounts countsByComment(@Param("commentId") UUID commentId);

    @Query("""
            select count(case when r.type = com.layoof.layoof.enums.ReactType.LIKE then 1 end) as likes,
                   count(case when r.type = com.layoof.layoof.enums.ReactType.DISLIKE then 1 end) as dislikes
            from React r
            where r.layoof.layoofId = :layoofId
            """)
    ReactCounts countsByLayoof(@Param("layoofId") UUID layoofId);
}
