package com.layoof.layoof.dto.response;

import com.layoof.layoof.enums.SourceType;

import java.util.UUID;

public record SourceResponseDto(
        UUID sourceId,
        String name,
        String feedUrl,
        SourceType type,
        String language,
        String region,
        String description
) {
}
