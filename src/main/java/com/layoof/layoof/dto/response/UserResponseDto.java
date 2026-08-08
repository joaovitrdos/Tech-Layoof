package com.layoof.layoof.dto.response;

import com.layoof.layoof.enums.AuthProvider;

import java.util.UUID;

public record UserResponseDto(
        UUID userId,
        String name,
        String email,
        String picture,
        AuthProvider authProvider
) {
}
