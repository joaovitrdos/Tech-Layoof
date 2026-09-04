package com.layoof.layoof.repository;

import com.layoof.layoof.entity.Layoof;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.enums.LayoofStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LayoofRepository extends JpaRepository<Layoof, UUID> {

    boolean existsBySourceUrl(String sourceUrl);

    boolean existsBySourceUrlAndLayoofIdNot(String sourceUrl, UUID layoofId);

    boolean existsByTitleFingerprint(String titleFingerprint);

    @EntityGraph(attributePaths = {"source", "author"})
    Optional<Layoof> findWithSourceAndAuthorByLayoofId(UUID layoofId);

    @EntityGraph(attributePaths = {"source", "author"})
    Page<Layoof> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = {"source", "author"})
    Page<Layoof> findByStatus(LayoofStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"source", "author"})
    List<Layoof> findByAuthorUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying
    @Query("UPDATE Layoof l SET l.author = null WHERE l.author = :author")
    void detachAuthor(@Param("author") User author);

    @Query("""
            select sum(case when r.type = com.layoof.layoof.enums.ReactType.LIKE then 1
                            when r.type = com.layoof.layoof.enums.ReactType.DISLIKE then -1
                            else 0 end)
            from Layoof l
            left join l.reacts r
            where l.author = :author
            group by l.layoofId, l.createdAt
            order by l.createdAt
            """)
    List<Long> reactBalancesByAuthor(@Param("author") User author);

    long countByStatus(LayoofStatus status);

    @Query(value = """
            select * 
            from tb_layoof l
            where 
                to_tsvector('portuguese', coalesce(l.title, ''))
                @@ plainto_tsquery('portuguese', :title)
            limit 20
            """, nativeQuery = true)
    List<Layoof> searchByTitle(@Param("title") String title);

    @EntityGraph(attributePaths = {"source", "author"})
    List<Layoof> findByLayoofIdIn(Collection<UUID> layoofIds);
}
