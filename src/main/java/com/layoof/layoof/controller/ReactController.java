package com.layoof.layoof.controller;

import com.layoof.layoof.dto.request.ReactRequestDto;
import com.layoof.layoof.dto.response.ReactResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.service.ReactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/comments/{commentId}/reacts")
@RequiredArgsConstructor
public class ReactController {

    private final ReactService reactService;

    @PostMapping
    public ReactResponseDto react(@PathVariable UUID commentId,
                                  @RequestBody @Valid ReactRequestDto request,
                                  @AuthenticationPrincipal User principal) {

        return reactService.react(commentId, request, principal);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID commentId, @AuthenticationPrincipal User principal) {
        reactService.remove(commentId, principal);
    }

    @GetMapping
    public List<ReactResponseDto> list(@PathVariable UUID commentId) {
        return reactService.listByComment(commentId);
    }

}
