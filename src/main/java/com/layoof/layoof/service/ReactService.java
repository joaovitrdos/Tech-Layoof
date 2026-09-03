package com.layoof.layoof.service;

import com.layoof.layoof.dto.request.ReactRequestDto;
import com.layoof.layoof.dto.response.ReactResponseDto;
import com.layoof.layoof.dto.response.ReactSummaryResponseDto;
import com.layoof.layoof.entity.Comment;
import com.layoof.layoof.entity.Layoof;
import com.layoof.layoof.entity.React;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.enums.ReactType;
import com.layoof.layoof.exception.CommentNotFoundException;
import com.layoof.layoof.exception.LayoofNotFoundException;
import com.layoof.layoof.exception.ReactConflictException;
import com.layoof.layoof.exception.ReactNotFoundException;
import com.layoof.layoof.exception.UnauthenticatedException;
import com.layoof.layoof.mapper.ReactMapper;
import com.layoof.layoof.repository.CommentRepository;
import com.layoof.layoof.repository.LayoofRepository;
import com.layoof.layoof.repository.ReactCounts;
import com.layoof.layoof.repository.ReactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactService {

    private final ReactRepository reactRepository;
    private final CommentRepository commentRepository;
    private final LayoofRepository layoofRepository;
    private final ReactMapper reactMapper;
    private final ReputationService reputationService;

    @Transactional
    public ReactResponseDto reactToComment(UUID commentId, ReactRequestDto request, User author) {
        requireAuthenticated(author);
        Comment comment = findCommentById(commentId);

        React react = findMyCommentReact(commentId, author)
                .orElseGet(() -> React.builder().comment(comment).author(author).build());
        react.setType(request.type());

        try {
            return reactMapper.toResponse(reactRepository.saveAndFlush(react));
        } catch (DataIntegrityViolationException ex) {
            throw new ReactConflictException(
                    "Ja existe uma reacao sua registrada no comentario: " + commentId, ex);
        }
    }

    @Transactional
    public ReactResponseDto reactToLayoof(UUID layoofId, ReactRequestDto request, User author) {
        requireAuthenticated(author);
        Layoof layoof = findLayoofById(layoofId);

        React react = findMyLayoofReact(layoofId, author)
                .orElseGet(() -> React.builder().layoof(layoof).author(author).build());
        react.setType(request.type());

        ReactResponseDto response;
        try {
            response = reactMapper.toResponse(reactRepository.saveAndFlush(react));
        } catch (DataIntegrityViolationException ex) {
            throw new ReactConflictException(
                    "Ja existe uma reacao sua registrada na demissao: " + layoofId, ex);
        }

        reputationService.refresh(layoof.getAuthor());

        return response;
    }

    @Transactional
    public void removeFromComment(UUID commentId, User author) {
        requireAuthenticated(author);

        React react = findMyCommentReact(commentId, author)
                .orElseThrow(() -> new ReactNotFoundException(
                        "Nenhuma reacao sua encontrada no comentario: " + commentId));

        reactRepository.delete(react);
    }

    @Transactional
    public void removeFromLayoof(UUID layoofId, User author) {
        requireAuthenticated(author);

        React react = findMyLayoofReact(layoofId, author)
                .orElseThrow(() -> new ReactNotFoundException(
                        "Nenhuma reacao sua encontrada na demissao: " + layoofId));

        Layoof layoof = react.getLayoof();
        reactRepository.delete(react);
        reactRepository.flush();

        reputationService.refresh(layoof.getAuthor());
    }

    @Transactional(readOnly = true)
    public ReactSummaryResponseDto summaryByComment(UUID commentId, User reader) {
        findCommentById(commentId);

        ReactCounts counts = reactRepository.countsByComment(commentId);
        ReactType myReact = reader == null ? null : findMyCommentReact(commentId, reader)
                .map(React::getType)
                .orElse(null);

        return new ReactSummaryResponseDto(counts.getLikes(), counts.getDislikes(), myReact);
    }

    @Transactional(readOnly = true)
    public ReactSummaryResponseDto summaryByLayoof(UUID layoofId, User reader) {
        findLayoofById(layoofId);

        ReactCounts counts = reactRepository.countsByLayoof(layoofId);
        ReactType myReact = reader == null ? null : findMyLayoofReact(layoofId, reader)
                .map(React::getType)
                .orElse(null);

        return new ReactSummaryResponseDto(counts.getLikes(), counts.getDislikes(), myReact);
    }

    private Optional<React> findMyCommentReact(UUID commentId, User author) {
        return reactRepository.findByCommentCommentIdAndAuthorUserId(commentId, author.getUserId());
    }

    private Optional<React> findMyLayoofReact(UUID layoofId, User author) {
        return reactRepository.findByLayoofLayoofIdAndAuthorUserId(layoofId, author.getUserId());
    }

    private Comment findCommentById(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(
                        "Nenhum comentario encontrado com o id: " + commentId));
    }

    private Layoof findLayoofById(UUID layoofId) {
        return layoofRepository.findById(layoofId)
                .orElseThrow(() -> new LayoofNotFoundException(
                        "Nenhuma demissao encontrada com o id: " + layoofId));
    }

    private void requireAuthenticated(User author) {
        if (author == null) {
            throw new UnauthenticatedException("E preciso estar autenticado para reagir");
        }
    }
}
