package com.layoof.layoof.repository;

import com.layoof.layoof.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SourceRepository extends JpaRepository<Source, UUID> {

    List<Source> findByActiveTrue();

    List<Source> findByActiveTrueAndLanguage(String language);

    /** Chave natural do catalogo: e por ela que a sincronizacao das fontes padrao e idempotente. */
    Optional<Source> findByFeedUrl(String feedUrl);

    boolean existsByFeedUrl(String feedUrl);
}
