package com.layoof.layoof.service;

import com.layoof.layoof.enums.LayoofStatus;
import com.layoof.layoof.repository.LayoofRepository;
import com.layoof.layoof.infra.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final LayoofRepository layoofRepository;

    @Cacheable(CacheConfig.SOURCE_COUNT)
    public long count() {
        return layoofRepository.countByStatus(LayoofStatus.PUBLISHED);
    }

}
