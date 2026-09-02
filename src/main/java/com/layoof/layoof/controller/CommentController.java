package com.layoof.layoof.controller;

import com.layoof.layoof.dto.request.CommentRequestDto;
import com.layoof.layoof.dto.request.UpdateCommentRequestDto;
import com.layoof.layoof.dto.response.CommentReponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentReponseDto create(@RequestBody @Valid CommentRequestDto request,
                                    @AuthenticationPrincipal User principal) {

        return commentService.create(request, principal);
    }

    @PutMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentReponseDto update(@PathVariable UUID commentId,
                                    @RequestBody @Valid UpdateCommentRequestDto request,
                                    @AuthenticationPrincipal User principal) {

        return commentService.update(commentId, request, principal);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID commentId, @AuthenticationPrincipal User principal) {
        commentService.delete(commentId, principal);
    }
}
