package com.layoof.layoof.dto.response;

import com.layoof.layoof.enums.ReactType;

import java.time.LocalDateTime;
import java.util.UUID;


public record ReactResponseDto(
        UUID reactId,
        ReactType type,
        UUID commentId,
        UUID authorId,
        String authorName,
        String authorPicture,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
