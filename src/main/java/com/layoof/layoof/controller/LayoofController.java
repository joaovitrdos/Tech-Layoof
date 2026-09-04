package com.layoof.layoof.controller;

import com.layoof.layoof.dto.request.LayoofRequestDto;
import com.layoof.layoof.dto.request.LayoofResearchRequestDto;
import com.layoof.layoof.dto.request.SearchLayoofRequestDto;
import com.layoof.layoof.dto.response.LayoofDraftResponseDto;
import com.layoof.layoof.dto.response.LayoofResponseDto;
import com.layoof.layoof.dto.response.SearchLayoofResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.enums.LayoofStatus;
import com.layoof.layoof.service.LayoofService;
import com.layoof.layoof.uploadFile.FileUploads;
import com.layoof.layoof.uploadFile.ValidImage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/layoofs")
@RequiredArgsConstructor
public class LayoofController {

    private final LayoofService layoofService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public LayoofResponseDto create(@RequestPart("data") @Valid LayoofRequestDto request,
                                    @RequestPart(value = "file", required = false) MultipartFile file,
                                    @AuthenticationPrincipal User principal) {

        return layoofService.create(request, file, principal);
    }

    @PostMapping("/ai")
    public LayoofDraftResponseDto research(@RequestBody @Valid LayoofResearchRequestDto request,
                                           @AuthenticationPrincipal User principal) {

        return layoofService.research(request, principal);
    }

    @GetMapping
    public Page<LayoofResponseDto> list(@RequestParam(required = false) LayoofStatus status,
                                        @PageableDefault(size = 20, sort = "publishedAt",
                                                direction = Sort.Direction.DESC) Pageable pageable) {

        return layoofService.list(status, pageable);
    }

    @GetMapping("/me")
    public List<LayoofResponseDto> listByAuthor(@AuthenticationPrincipal User principal) {
        return layoofService.listByAuthor(principal);
    }

    @GetMapping("/search")
    public List<SearchLayoofResponseDto> searchLayoof(@Valid @ModelAttribute SearchLayoofRequestDto request) {
        return layoofService.searchLayoof(request.title());
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

    @PostMapping(path = "/{layoofId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LayoofResponseDto uploadImage(@PathVariable UUID layoofId,
                                         @ValidImage @RequestPart("file") MultipartFile file,
                                         @AuthenticationPrincipal User principal) {

        return layoofService.updateImage(layoofId, principal, FileUploads.from(file));
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
