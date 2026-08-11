package com.layoof.layoof.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Edicao de comentario.
 *
 * <p>So o texto muda. Nao aceita {@code layoofId}: mover um comentario para outra demissao
 * mudaria o contexto em que ele foi escrito, e nao ha caso de uso para isso.
 */
public record UpdateCommentRequestDto(

        @NotBlank(message = "O conteudo do comentario e obrigatorio")
        @Size(max = 2000, message = "O comentario deve ter no maximo 2000 caracteres")
        String content
) {
}
