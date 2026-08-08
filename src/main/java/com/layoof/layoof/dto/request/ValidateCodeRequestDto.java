package com.layoof.layoof.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ValidateCodeRequestDto(

        @NotBlank(message = "O codigo e obrigatorio")
        @Pattern(regexp = "\\d{6}", message = "O codigo deve ter 6 digitos")
        String code,

        @NotBlank(message = "O email e obrigatorio")
        @Email(message = "Email invalido")
        String email
) {
}
