package com.layoof.layoof.dto.response;

import com.layoof.layoof.enums.LayoofConfidence;

import java.time.LocalDateTime;

public record LayoofDraftResponseDto(
        String company,
        String title,
        Integer numbersOfCuts,
        String city,
        String country,
        String summary,
        String content,
        String imageUrl,
        String sourceUrl,
        LocalDateTime publishedAt,
        LayoofConfidence confidence,
        SourceDraftResponseDto source
) {
}
