package com.layoof.layoof.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchLayoofRequestDto(

        @NotBlank(message = "O termo de pesquisa e obrigatorio")
        @Size(min = 2, message = "A pesquisa deve ter no minimo 2 caracteres")
        String title
) {
}
