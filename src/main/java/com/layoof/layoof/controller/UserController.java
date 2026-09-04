package com.layoof.layoof.controller;

import com.layoof.layoof.dto.request.ChangePasswordRequestDto;
import com.layoof.layoof.dto.request.LayoofRequestDto;
import com.layoof.layoof.dto.request.SearchUserRequestDto;
import com.layoof.layoof.dto.request.UpdateUserRequestDto;
import com.layoof.layoof.dto.response.CommentReponseDto;
import com.layoof.layoof.dto.response.LayoofResponseDto;
import com.layoof.layoof.dto.response.ReactResponseDto;
import com.layoof.layoof.dto.response.PublicUserResponseDto;
import com.layoof.layoof.dto.response.ResetPasswordResponseDto;
import com.layoof.layoof.dto.response.SearchUserResponseDto;
import com.layoof.layoof.dto.response.UserResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.service.CommentService;
import com.layoof.layoof.service.LayoofService;
import com.layoof.layoof.service.ReactService;
import com.layoof.layoof.service.PasswordRecoveryService;
import com.layoof.layoof.service.UserService;
import com.layoof.layoof.uploadFile.FileUploads;
import com.layoof.layoof.uploadFile.ValidImage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordRecoveryService passwordRecoveryService;
    private final LayoofService layoofService;
    private final CommentService commentService;
    private final ReactService reactService;

    @GetMapping("/me")
    public UserResponseDto me(@AuthenticationPrincipal User principal) {
        return userService.findById(principal.getUserId());
    }

    @PutMapping(path = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDto update(@AuthenticationPrincipal User principal,
                                  @RequestPart("data") @Valid UpdateUserRequestDto request,
                                  @RequestPart(value = "file", required = false) MultipartFile file) {

        return userService.updateProfile(principal.getUserId(), request, file);
    }

    @PostMapping(path = "/me/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDto uploadPicture(@AuthenticationPrincipal User principal,
                                         @ValidImage @RequestPart("file") MultipartFile file) {

        return userService.updatePicture(principal.getUserId(), principal, FileUploads.from(file));
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User principal) {
        userService.delete(principal.getUserId());
    }

    @GetMapping("/me/layoofs")
    public List<LayoofResponseDto> myLayoofs(@AuthenticationPrincipal User principal) {
        return layoofService.listByAuthor(principal);
    }

    @GetMapping("/search")
    public List<SearchUserResponseDto> searchUser(@Valid @ModelAttribute SearchUserRequestDto request,
                                                 @AuthenticationPrincipal User principal) {

        return userService.searchUser(request.name(), principal == null ? null : principal.getUserId());
    }

    @GetMapping("/{userId}")
    public PublicUserResponseDto findById(@PathVariable UUID userId) {
        return userService.findPublicById(userId);
    }

    @GetMapping("/{userId}/layoofs")
    public List<LayoofResponseDto> layoofsByUser(@PathVariable UUID userId) {
        return layoofService.listByAuthorId(userId);
    }

    @GetMapping("/{userId}/comments")
    public List<CommentReponseDto> commentsByUser(@PathVariable UUID userId) {
        return commentService.listByAuthorId(userId);
    }

    @GetMapping("/{userId}/reacts")
    public List<ReactResponseDto> reactsByUser(@PathVariable UUID userId) {
        return reactService.listByAuthorId(userId);
    }

    @PostMapping("/change-password")
    public ResetPasswordResponseDto changePassword(@AuthenticationPrincipal User loggedInUser, @RequestBody @Valid ChangePasswordRequestDto request) {
        return passwordRecoveryService.changePassword(loggedInUser, request);
    }

    @GetMapping("/count")
    public long count() {
        return userService.count();
    }
}
