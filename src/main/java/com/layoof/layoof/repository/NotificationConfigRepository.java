package com.layoof.layoof.repository;

import com.layoof.layoof.entity.NotificationConfig;
import com.layoof.layoof.enums.NotificationFrequency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationConfigRepository extends JpaRepository<NotificationConfig, UUID> {

    Optional<NotificationConfig> findByUserUserId(UUID userId);

    @Query("""
            select config from NotificationConfig config
            join fetch config.user
            where config.frequency in :frequencies
            """)
    List<NotificationConfig> findByFrequencyIn(
            @Param("frequencies") Collection<NotificationFrequency> frequencies);
}
