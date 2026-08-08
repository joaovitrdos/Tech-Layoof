package com.layoof.layoof.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendEmailRequestDto(

        @NotBlank(message = "O email e obrigatorio")
        @Email(message = "Email invalido")
        String email
) {
}
