package com.layoof.layoof.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentReponseDto(
        UUID id,
        String content,
        UUID layoofId,
        UUID authorId,
        String authorName,
        String authorPicture,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
