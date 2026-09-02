package com.layoof.layoof.dto.response;

import com.layoof.layoof.enums.LayoofConfidence;
import com.layoof.layoof.enums.LayoofStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record LayoofResponseDto(
        UUID layoofId,
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
        LayoofStatus status,
        LayoofConfidence confidence,
        SourceResponseDto source,
        UUID authorId,
        String authorName,
        String authorPicture,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
