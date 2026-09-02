package com.layoof.layoof.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LayoofResearchRequestDto(

        @NotBlank(message = "Informe a empresa ou o endereco da noticia a pesquisar")
        String query
) {
}
