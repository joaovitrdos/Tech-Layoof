package com.layoof.layoof.dto.response;

import com.layoof.layoof.enums.UserConfidence;

import java.util.UUID;

public record PublicUserResponseDto(
        UUID userId,
        String name,
        String picture,
        int confidenceScore,
        UserConfidence confidence,
        int badges
) {
}
