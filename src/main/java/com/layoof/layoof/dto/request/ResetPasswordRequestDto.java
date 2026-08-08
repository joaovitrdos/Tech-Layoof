package com.layoof.layoof.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(

        @NotBlank(message = "O codigo e obrigatorio")
        @Pattern(regexp = "\\d{6}", message = "O codigo deve ter 6 digitos")
        String code,

        @NotBlank(message = "O email e obrigatorio")
        @Email(message = "Email invalido")
        String email,

        @NotBlank(message = "A senha e obrigatoria")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String newPassword
) {
}
