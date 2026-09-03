package com.layoof.layoof.dto.response;

import com.layoof.layoof.enums.ReactType;

public record ReactSummaryResponseDto(
        long likes,
        long dislikes,
        ReactType myReact
) {
}
