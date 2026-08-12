package com.layoof.layoof.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(
        @NotBlank(message = "A senha atual e obrigatoria")
        String currentPassword,

        @NotBlank(message = "A senha nova e obrigatoria")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String newPassword
) {
}
