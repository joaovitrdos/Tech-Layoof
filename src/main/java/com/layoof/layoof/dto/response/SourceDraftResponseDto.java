package com.layoof.layoof.dto.response;

import com.layoof.layoof.enums.SourceType;

public record SourceDraftResponseDto(
        String name,
        String feedUrl,
        SourceType type,
        String language,
        String region,
        String description
) {
}
