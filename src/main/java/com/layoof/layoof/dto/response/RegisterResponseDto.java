package com.layoof.layoof.dto.response;

import com.layoof.layoof.enums.AuthProvider;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterResponseDto(
        String name,
        String email,
        AuthProvider authProvider,
        LocalDateTime createdAt
) {
}
