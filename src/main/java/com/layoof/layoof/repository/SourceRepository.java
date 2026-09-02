package com.layoof.layoof.repository;

import com.layoof.layoof.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SourceRepository extends JpaRepository<Source, UUID> {

    Optional<Source> findByFeedUrl(String feedUrl);

    @Query("SELECT s FROM Source s WHERE s.active = true "
            + "ORDER BY s.lastFetchedAt ASC NULLS FIRST, s.name ASC")
    List<Source> findActiveByLeastRecentlyFetched();
}
