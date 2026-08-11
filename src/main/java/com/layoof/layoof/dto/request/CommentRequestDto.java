package com.layoof.layoof.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;


public record CommentRequestDto(

        @NotBlank(message = "O conteudo do comentario e obrigatorio")
        @Size(max = 2000, message = "O comentario deve ter no maximo 2000 caracteres")
        String content,

        @NotNull(message = "O comentario precisa estar vinculado a uma demissao")
        UUID layoofId
) {
}
