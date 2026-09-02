package com.layoof.layoof.service;

import com.layoof.layoof.dto.request.CommentRequestDto;
import com.layoof.layoof.dto.request.UpdateCommentRequestDto;
import com.layoof.layoof.dto.response.CommentReponseDto;
import com.layoof.layoof.entity.Comment;
import com.layoof.layoof.entity.Layoof;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.exception.CommentNotFoundException;
import com.layoof.layoof.exception.CommentNotOwnedException;
import com.layoof.layoof.exception.CommentWindowExpiredException;
import com.layoof.layoof.exception.LayoofNotFoundException;
import com.layoof.layoof.exception.UnauthenticatedException;
import com.layoof.layoof.mapper.CommentMapper;
import com.layoof.layoof.repository.CommentRepository;
import com.layoof.layoof.repository.LayoofRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final Duration EDIT_WINDOW = Duration.ofMinutes(5);
    private static final Duration DELETE_WINDOW = Duration.ofMinutes(10);

    private final CommentRepository commentRepository;
    private final LayoofRepository layoofRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentReponseDto create(CommentRequestDto request, User author) {
        requireAuthenticated(author);

        Layoof layoof = layoofRepository.findById(request.layoofId())
                .orElseThrow(() -> new LayoofNotFoundException(
                        "Nenhuma demissao encontrada com o id: " + request.layoofId()));

        Comment comment = Comment.builder()
                .content(request.content().strip())
                .layoof(layoof)
                .author(author)
                .build();

        Comment saved = commentRepository.save(comment);

        return commentMapper.toResponse(saved);
    }

    @Transactional
    public CommentReponseDto update(UUID commentId, UpdateCommentRequestDto request, User author) {
        requireAuthenticated(author);

        Comment comment = requireAuthor(findEntityById(commentId), author,
                "Voce so pode editar os seus proprios comentarios");

        requireWindowOpen(comment, EDIT_WINDOW,
                "Comentarios so podem ser editados nos primeiros %d minutos apos a criacao");

        comment.setContent(request.content().strip());

        Comment saved = commentRepository.save(comment);

        return commentMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID commentId, User author) {
        requireAuthenticated(author);

        Comment comment = requireAuthor(findEntityById(commentId), author,
                "Voce so pode apagar os seus proprios comentarios");

        requireWindowOpen(comment, DELETE_WINDOW,
                "Comentarios so podem ser apagados nos primeiros %d minutos apos a criacao");

        commentRepository.delete(comment);
    }

    private void requireWindowOpen(Comment comment, Duration window, String message) {
        LocalDateTime deadline = comment.getCreatedAt().plus(window);

        if (LocalDateTime.now().isAfter(deadline)) {
            throw new CommentWindowExpiredException(message.formatted(window.toMinutes()));
        }
    }

    private Comment findEntityById(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(
                        "Nenhum comentario encontrado com o id: " + commentId));
    }

    private Comment requireAuthor(Comment comment, User author, String message) {
        if (!comment.isAuthoredBy(author)) {
            throw new CommentNotOwnedException(message);
        }
        return comment;
    }

    private void requireAuthenticated(User author) {
        if (author == null) {
            throw new UnauthenticatedException("E preciso estar autenticado para comentar");
        }
    }
}
