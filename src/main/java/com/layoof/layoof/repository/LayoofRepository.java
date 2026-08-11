package com.layoof.layoof.repository;

import com.layoof.layoof.entity.Layoof;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LayoofRepository extends JpaRepository<Layoof, UUID> {

    /** Deduplicacao por artigo. Consultado antes de qualquer chamada de IA, para nao gastar token. */
    boolean existsBySourceUrl(String sourceUrl);

    Page<Layoof> findAllByOrderByPublishedAtDesc(Pageable pageable);

    /**
     * Deduplicacao semantica: a mesma demissao sai em dezenas de veiculos com URLs diferentes.
     * Sem esta checagem a home mostra oito vezes o mesmo corte da Amazon.
     */
    List<Layoof> findByCompanyIgnoreCaseAndPublishedAtBetween(String company,
                                                              LocalDateTime inicio,
                                                              LocalDateTime fim);

    boolean existsByCompanyIgnoreCaseAndPublishedAtBetween(String company,
                                                           LocalDateTime inicio,
                                                           LocalDateTime fim);
}
