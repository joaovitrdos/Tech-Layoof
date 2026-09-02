package com.layoof.layoof.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record LayoofRequestDto(

        @NotBlank(message = "A empresa e obrigatoria")
        String company,

        @NotBlank(message = "O titulo e obrigatorio")
        String title,

        Integer numbersOfCuts,

        String city,

        String country,

        String summary,

        String content,

        @Pattern(regexp = "^$|^https?://\\S+$", message = "O endereco da imagem deve ser http ou https")
        String imageUrl,

        @NotBlank(message = "O endereco da noticia e obrigatorio")
        @Pattern(regexp = "^https?://\\S+$", message = "O endereco da noticia deve ser http ou https")
        String sourceUrl,

        @PastOrPresent(message = "A data de publicacao nao pode estar no futuro")
        LocalDateTime publishedAt,

        @NotNull(message = "O veiculo da noticia e obrigatorio")
        @Valid
        SourceRequestDto source
) {
}
