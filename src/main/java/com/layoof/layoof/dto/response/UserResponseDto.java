package com.layoof.layoof.dto.response;

import com.layoof.layoof.enums.AuthProvider;
import com.layoof.layoof.enums.UserConfidence;

import java.util.UUID;

public record UserResponseDto(
        String name,
        String email,
        String picture,
        AuthProvider authProvider,
        int confidenceScore,
        UserConfidence confidence,
        int badges
) {
}
