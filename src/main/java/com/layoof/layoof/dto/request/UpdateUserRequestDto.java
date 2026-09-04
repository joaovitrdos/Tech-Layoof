package com.layoof.layoof.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDto(

        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
        String name,

        @Size(max = 512, message = "A foto deve ter no maximo 512 caracteres")
        @Pattern(regexp = "^$|^https?://\\S+$", message = "A foto deve ser um endereco http ou https")
        String picture,

        String linkedinURL
) {
}
