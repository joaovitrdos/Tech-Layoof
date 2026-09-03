package com.layoof.layoof.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchUserRequestDto(
        @Size(min = 2)
        @NotBlank(message = "O nome e obrigatorio")
        String name
) {
}
