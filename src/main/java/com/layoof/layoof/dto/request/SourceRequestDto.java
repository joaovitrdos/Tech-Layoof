package com.layoof.layoof.dto.request;

import com.layoof.layoof.enums.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SourceRequestDto(

        @NotBlank(message = "O nome do veiculo e obrigatorio")
        String name,

        @NotBlank(message = "O endereco do veiculo e obrigatorio")
        @Pattern(regexp = "^https?://\\S+$", message = "O endereco do veiculo deve ser http ou https")
        String feedUrl,

        @NotNull(message = "O tipo do veiculo e obrigatorio")
        SourceType type,

        String language,

        String region,

        String description
) {
}
