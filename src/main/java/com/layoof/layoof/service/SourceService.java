package com.layoof.layoof.service;

import com.layoof.layoof.enums.LayoofStatus;
import com.layoof.layoof.repository.LayoofRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final LayoofRepository layoofRepository;

    public long count() {
        return layoofRepository.countByStatus(LayoofStatus.PUBLISHED);
    }

}
