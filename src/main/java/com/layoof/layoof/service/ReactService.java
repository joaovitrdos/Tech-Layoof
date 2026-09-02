package com.layoof.layoof.service;

import com.layoof.layoof.dto.request.ReactRequestDto;
import com.layoof.layoof.dto.response.ReactResponseDto;
import com.layoof.layoof.entity.Comment;
import com.layoof.layoof.entity.React;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.exception.CommentNotFoundException;
import com.layoof.layoof.exception.ReactConflictException;
import com.layoof.layoof.exception.ReactNotFoundException;
import com.layoof.layoof.exception.UnauthenticatedException;
import com.layoof.layoof.mapper.ReactMapper;
import com.layoof.layoof.repository.CommentRepository;
import com.layoof.layoof.repository.ReactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactService {

    private final ReactRepository reactRepository;
    private final CommentRepository commentRepository;
    private final ReactMapper reactMapper;

    @Transactional
    public ReactResponseDto react(UUID commentId, ReactRequestDto request, User author) {
        requireAuthenticated(author);
        Comment comment = findCommentById(commentId);

        React react = findMyReact(commentId, author)
                .orElseGet(() -> React.builder()
                        .comment(comment)
                        .author(author)
                        .build());

        react.setType(request.type());

        return reactMapper.toResponse(save(react, commentId));
    }

    @Transactional
    public void remove(UUID commentId, User author) {
        requireAuthenticated(author);

        React react = findMyReact(commentId, author)
                .orElseThrow(() -> new ReactNotFoundException(
                        "Nenhuma reacao sua encontrada no comentario: " + commentId));

        reactRepository.delete(react);
    }

    @Transactional(readOnly = true)
    public List<ReactResponseDto> listByComment(UUID commentId) {
        findCommentById(commentId);
        return reactMapper.toResponseList(reactRepository.findByCommentCommentIdOrderByCreatedAtDesc(commentId));
    }

    private React save(React react, UUID commentId) {
        try {
            return reactRepository.saveAndFlush(react);
        } catch (DataIntegrityViolationException ex) {
            throw new ReactConflictException(
                    "Ja existe uma reacao sua registrada no comentario: " + commentId, ex);
        }
    }

    private Optional<React> findMyReact(UUID commentId, User author) {
        return reactRepository.findByCommentCommentIdAndAuthorUserId(commentId, author.getUserId());
    }

    private Comment findCommentById(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(
                        "Nenhum comentario encontrado com o id: " + commentId));
    }

    private void requireAuthenticated(User author) {
        if (author == null) {
            throw new UnauthenticatedException("E preciso estar autenticado para reagir a um comentario");
        }
    }
}
