package com.layoof.layoof.service;

import com.layoof.layoof.dto.request.CommentRequestDto;
import com.layoof.layoof.dto.request.UpdateCommentRequestDto;
import com.layoof.layoof.dto.response.CommentReponseDto;
import com.layoof.layoof.entity.Comment;
import com.layoof.layoof.entity.Layoof;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.exception.CommentNotFoundException;
import com.layoof.layoof.exception.CommentNotOwnedException;
import com.layoof.layoof.exception.LayoofNotFoundException;
import com.layoof.layoof.mapper.CommentMapper;
import com.layoof.layoof.repository.CommentRepository;
import com.layoof.layoof.repository.LayoofRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final LayoofRepository layoofRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentReponseDto create(CommentRequestDto request, User author) {
        Layoof layoof = layoofRepository.findById(request.layoofId())
                .orElseThrow(() -> LayoofNotFoundException.byId(request.layoofId()));

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
        Comment comment = requireAuthor(findEntityById(commentId), author);

        validateEditTempComment(comment);
        comment.setContent(request.content().strip());

        Comment saved = commentRepository.save(comment);

        return commentMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID commentId, User author) {
        Comment comment = requireAuthor(findEntityById(commentId), author);
        validateDeleteTempComment(comment);
        commentRepository.delete(comment);
    }

    private void validateEditTempComment(Comment comment) {
        LocalDateTime editLimit = comment.getCreatedAt().plusMinutes(5);

        if (LocalDateTime.now().isAfter(editLimit)) {
            throw new RuntimeException("O tempo limite de 5 minutos para editar expirou");
        }
    }

    private void validateDeleteTempComment(Comment comment) {
        LocalDateTime deletLimit = comment.getCreatedAt().plusMinutes(10);

        if (LocalDateTime.now().isAfter(deletLimit)) {
            throw new RuntimeException("O tempo limite de 10 minutos para excluir expirou");
        }
    }

    private Comment findEntityById(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> CommentNotFoundException.byId(commentId));
    }

    private Comment requireAuthor(Comment comment, User author) {
        if (!comment.isAuthoredBy(author)) {
            throw new CommentNotOwnedException();
        }
        return comment;
    }
}