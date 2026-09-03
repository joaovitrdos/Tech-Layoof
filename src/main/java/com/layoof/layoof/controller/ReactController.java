package com.layoof.layoof.controller;

import com.layoof.layoof.dto.request.ReactRequestDto;
import com.layoof.layoof.dto.response.ReactResponseDto;
import com.layoof.layoof.dto.response.ReactSummaryResponseDto;
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

import java.util.UUID;

@RestController
@RequestMapping("/reacts")
@RequiredArgsConstructor
public class ReactController {

    private final ReactService reactService;

    @PostMapping("/comments/{commentId}")
    public ReactResponseDto reactToComment(@PathVariable UUID commentId,
                                           @RequestBody @Valid ReactRequestDto request,
                                           @AuthenticationPrincipal User principal) {

        return reactService.reactToComment(commentId, request, principal);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromComment(@PathVariable UUID commentId,
                                  @AuthenticationPrincipal User principal) {

        reactService.removeFromComment(commentId, principal);
    }

    @GetMapping("/comments/{commentId}")
    public ReactSummaryResponseDto summaryByComment(@PathVariable UUID commentId,
                                                    @AuthenticationPrincipal User principal) {

        return reactService.summaryByComment(commentId, principal);
    }

    @PostMapping("/layoofs/{layoofId}")
    public ReactResponseDto reactToLayoof(@PathVariable UUID layoofId,
                                          @RequestBody @Valid ReactRequestDto request,
                                          @AuthenticationPrincipal User principal) {

        return reactService.reactToLayoof(layoofId, request, principal);
    }

    @DeleteMapping("/layoofs/{layoofId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromLayoof(@PathVariable UUID layoofId,
                                 @AuthenticationPrincipal User principal) {

        reactService.removeFromLayoof(layoofId, principal);
    }

    @GetMapping("/layoofs/{layoofId}")
    public ReactSummaryResponseDto summaryByLayoof(@PathVariable UUID layoofId,
                                                   @AuthenticationPrincipal User principal) {

        return reactService.summaryByLayoof(layoofId, principal);
    }
}
