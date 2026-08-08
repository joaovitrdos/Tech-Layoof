package com.layoof.layoof.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequestDto(

        @NotBlank(message = "O token do Google e obrigatorio")
        String idToken
) {
}
