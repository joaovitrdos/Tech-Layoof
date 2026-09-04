package com.layoof.layoof.dto.response;

import java.util.UUID;

public record SearchUserResponseDto(
        UUID userId,
        String name,
        String picture,
        int badges
) {
}
