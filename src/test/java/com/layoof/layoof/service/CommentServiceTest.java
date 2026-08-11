package com.layoof.layoof.service;

import com.layoof.layoof.dto.request.CommentRequestDto;
import com.layoof.layoof.dto.request.UpdateCommentRequestDto;
import com.layoof.layoof.entity.Comment;
import com.layoof.layoof.entity.Layoof;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.exception.CommentNotFoundException;
import com.layoof.layoof.exception.CommentNotOwnedException;
import com.layoof.layoof.exception.LayoofNotFoundException;
import com.layoof.layoof.mapper.CommentMapper;
import com.layoof.layoof.repository.CommentRepository;
import com.layoof.layoof.repository.LayoofRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes do {@link CommentService}.
 *
 * <p>Descrevem o comportamento atual do service. Dois pontos merecem atencao de quem for mexer:
 *
 * <ul>
 *   <li><b>Os prazos estao invertidos em relacao a regra combinada.</b> Hoje
 *       {@code validateEditTempComment} usa 10 minutos e {@code validateDeleteTempComment} usa 5 —
 *       a regra pedida era editar em 5 e apagar em 10. Os testes abaixo travam o que o codigo faz;
 *       se os numeros forem corrigidos no service, e aqui que a mudanca vai aparecer.</li>
 *   <li>O prazo estourado sobe {@code RuntimeException} pura, que o {@code GlobalExceptionHandler}
 *       traduz em 500 generico. Por isso as asserções usam {@code isExactlyInstanceOf}: se um dia
 *       virar excecao tipada, o teste avisa em vez de passar por acidente, ja que toda excecao do
 *       projeto tambem e {@code RuntimeException}.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService")
class CommentServiceTest {

    private static final UUID COMMENT_ID = UUID.randomUUID();
    private static final UUID LAYOOF_ID = UUID.randomUUID();

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private LayoofRepository layoofRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    @Captor
    private ArgumentCaptor<Comment> commentCaptor;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("grava o autor recebido do principal e limpa o espaco em volta do texto")
        void deveGravarComOAutorRecebido() {
            User autor = user();
            when(layoofRepository.findById(LAYOOF_ID)).thenReturn(Optional.of(layoof()));
            when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));

            commentService.create(new CommentRequestDto("  Triste noticia  ", LAYOOF_ID), autor);

            verify(commentRepository).save(commentCaptor.capture());
            assertThat(commentCaptor.getValue().getAuthor()).isSameAs(autor);
            assertThat(commentCaptor.getValue().getContent()).isEqualTo("Triste noticia");
            assertThat(commentCaptor.getValue().getLayoof().getId()).isEqualTo(LAYOOF_ID);
        }

        @Test
        @DisplayName("recusa comentario em demissao inexistente, sem gravar nada")
        void deveRecusarLayoofInexistente() {
            when(layoofRepository.findById(LAYOOF_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    commentService.create(new CommentRequestDto("oi", LAYOOF_ID), user()))
                    .isInstanceOf(LayoofNotFoundException.class);

            verify(commentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("o autor edita dentro do prazo e o texto e gravado sem espaco em volta")
        void deveEditarDentroDoPrazo() {
            User autor = user();
            when(commentRepository.findById(COMMENT_ID))
                    .thenReturn(Optional.of(comment(autor, minutosAtras(9))));
            when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));

            commentService.update(COMMENT_ID, new UpdateCommentRequestDto("  texto novo  "), autor);

            verify(commentRepository).save(commentCaptor.capture());
            assertThat(commentCaptor.getValue().getContent()).isEqualTo("texto novo");
        }

        @Test
        @DisplayName("passado o prazo de edicao, nao edita mais")
        void deveRecusarForaDoPrazo() {
            User autor = user();
            when(commentRepository.findById(COMMENT_ID))
                    .thenReturn(Optional.of(comment(autor, minutosAtras(11))));

            assertThatThrownBy(() ->
                    commentService.update(COMMENT_ID, new UpdateCommentRequestDto("x"), autor))
                    .isExactlyInstanceOf(RuntimeException.class)
                    .hasMessage("O tempo limite de 10 minutos para excluir expirou");

            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("comentario inexistente da 404, e nao 403")
        void deveFalharQuandoNaoExiste() {
            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    commentService.update(COMMENT_ID, new UpdateCommentRequestDto("x"), user()))
                    .isInstanceOf(CommentNotFoundException.class);

            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("nao edita comentario de outra pessoa")
        void deveFalharQuandoNaoEhOAutor() {
            when(commentRepository.findById(COMMENT_ID))
                    .thenReturn(Optional.of(comment(user(), minutosAtras(1))));

            assertThatThrownBy(() ->
                    commentService.update(COMMENT_ID, new UpdateCommentRequestDto("x"), outroUser()))
                    .isInstanceOf(CommentNotOwnedException.class);

            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("a posse e checada antes do prazo: comentario alheio e vencido da erro de posse")
        void deveChecarPosseAntesDoPrazo() {
            when(commentRepository.findById(COMMENT_ID))
                    .thenReturn(Optional.of(comment(user(), minutosAtras(60))));

            assertThatThrownBy(() ->
                    commentService.update(COMMENT_ID, new UpdateCommentRequestDto("x"), outroUser()))
                    .isInstanceOf(CommentNotOwnedException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("o autor apaga dentro do prazo")
        void deveApagarDentroDoPrazo() {
            User autor = user();
            Comment comment = comment(autor, minutosAtras(4));
            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

            commentService.delete(COMMENT_ID, autor);

            verify(commentRepository).delete(comment);
        }

        @Test
        @DisplayName("passado o prazo de exclusao, nao apaga mais")
        void deveRecusarForaDoPrazo() {
            User autor = user();
            when(commentRepository.findById(COMMENT_ID))
                    .thenReturn(Optional.of(comment(autor, minutosAtras(6))));

            assertThatThrownBy(() -> commentService.delete(COMMENT_ID, autor))
                    .isExactlyInstanceOf(RuntimeException.class)
                    .hasMessage("O tempo limite de 5 minutos para edição expirou");

            verify(commentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("comentario inexistente da 404, e nao 403")
        void deveFalharQuandoNaoExiste() {
            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.delete(COMMENT_ID, user()))
                    .isInstanceOf(CommentNotFoundException.class);

            verify(commentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("nao apaga comentario de outra pessoa")
        void deveFalharQuandoNaoEhOAutor() {
            when(commentRepository.findById(COMMENT_ID))
                    .thenReturn(Optional.of(comment(user(), minutosAtras(1))));

            assertThatThrownBy(() -> commentService.delete(COMMENT_ID, outroUser()))
                    .isInstanceOf(CommentNotOwnedException.class);

            verify(commentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("a posse e checada antes do prazo: comentario alheio e vencido da erro de posse")
        void deveChecarPosseAntesDoPrazo() {
            when(commentRepository.findById(COMMENT_ID))
                    .thenReturn(Optional.of(comment(user(), minutosAtras(60))));

            assertThatThrownBy(() -> commentService.delete(COMMENT_ID, outroUser()))
                    .isInstanceOf(CommentNotOwnedException.class);
        }
    }

    // ------------------------------------------------------------------

    private LocalDateTime minutosAtras(int minutos) {
        return LocalDateTime.now().minusMinutes(minutos);
    }

    private User user() {
        User user = new User();
        user.setUserId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        return user;
    }

    private User outroUser() {
        User user = new User();
        user.setUserId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        return user;
    }

    private Layoof layoof() {
        Layoof layoof = new Layoof();
        layoof.setId(LAYOOF_ID);
        return layoof;
    }

    private Comment comment(User autor, LocalDateTime criadoEm) {
        Comment comment = Comment.builder()
                .id(COMMENT_ID)
                .content("texto original")
                .layoof(layoof())
                .author(autor)
                .build();
        comment.setCreatedAt(criadoEm);
        return comment;
    }
}
