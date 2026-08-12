package com.layoof.layoof.dto.request;

import com.layoof.layoof.enums.ReactType;
import jakarta.validation.constraints.NotNull;

public record ReactRequestDto(

        @NotNull(message = "O tipo da reacao e obrigatorio")
        ReactType type
) {
}
