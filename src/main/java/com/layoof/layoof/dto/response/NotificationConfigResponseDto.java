package com.layoof.layoof.dto.response;

import com.layoof.layoof.enums.NotificationFrequency;

import java.time.LocalDateTime;

public record NotificationConfigResponseDto(
        NotificationFrequency frequency,
        LocalDateTime lastSentAt,
        LocalDateTime updatedAt
) {
}
