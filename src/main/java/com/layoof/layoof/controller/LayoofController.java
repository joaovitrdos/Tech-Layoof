package com.layoof.layoof.controller;

import com.layoof.layoof.dto.request.LayoofRequestDto;
import com.layoof.layoof.dto.request.LayoofResearchRequestDto;
import com.layoof.layoof.dto.response.LayoofDraftResponseDto;
import com.layoof.layoof.dto.response.LayoofResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.enums.LayoofStatus;
import com.layoof.layoof.service.LayoofService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/layoofs")
@RequiredArgsConstructor
public class LayoofController {

    private final LayoofService layoofService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LayoofResponseDto create(@RequestBody @Valid LayoofRequestDto request,
                                    @AuthenticationPrincipal User principal) {

        return layoofService.create(request, principal);
    }

    @PostMapping("/ai")
    public LayoofDraftResponseDto research(@RequestBody @Valid LayoofResearchRequestDto request,
                                           @AuthenticationPrincipal User principal) {

        return layoofService.research(request, principal);
    }

    @GetMapping
    public List<LayoofResponseDto> list(@RequestParam(required = false) LayoofStatus status) {
        return layoofService.list(status);
    }

    @GetMapping("/me")
    public List<LayoofResponseDto> listByAuthor(@AuthenticationPrincipal User principal) {
        return layoofService.listByAuthor(principal);
    }

    @GetMapping("/{layoofId}")
    public LayoofResponseDto findById(@PathVariable UUID layoofId) {
        return layoofService.findById(layoofId);
    }

    @PutMapping("/{layoofId}")
    @ResponseStatus(HttpStatus.OK)
    public LayoofResponseDto update(@PathVariable UUID layoofId,
                                    @RequestBody @Valid LayoofRequestDto request,
                                    @AuthenticationPrincipal User principal) {

        return layoofService.update(layoofId, request, principal);
    }

    @DeleteMapping("/{layoofId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID layoofId, @AuthenticationPrincipal User principal) {
        layoofService.delete(layoofId, principal);
    }

    @GetMapping("/count")
    public long count() {
        return layoofService.count();
    }
}
