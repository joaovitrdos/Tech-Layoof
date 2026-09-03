package com.layoof.layoof.controller;

import com.layoof.layoof.dto.request.ChangePasswordRequestDto;
import com.layoof.layoof.dto.request.LayoofRequestDto;
import com.layoof.layoof.dto.request.SearchUserRequestDto;
import com.layoof.layoof.dto.request.UpdateUserRequestDto;
import com.layoof.layoof.dto.response.LayoofResponseDto;
import com.layoof.layoof.dto.response.PublicUserResponseDto;
import com.layoof.layoof.dto.response.ResetPasswordResponseDto;
import com.layoof.layoof.dto.response.SearchUserResponseDto;
import com.layoof.layoof.dto.response.UserResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.service.LayoofService;
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

    @GetMapping("/me")
    public UserResponseDto me(@AuthenticationPrincipal User principal) {
        return userService.findById(principal.getUserId());
    }

    @PutMapping("/me")
    public UserResponseDto updateMe(@AuthenticationPrincipal User principal, @RequestBody @Valid UpdateUserRequestDto request) {
        return userService.updateProfile(principal.getUserId(), request);
    }

    @PostMapping(path = "/me/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDto uploadPicture(@AuthenticationPrincipal User principal,
                                         @ValidImage @RequestPart("file") MultipartFile file) {

        return userService.updatePicture(principal.getUserId(), FileUploads.from(file));
    }

    @DeleteMapping("/me/picture")
    public UserResponseDto deletePicture(@AuthenticationPrincipal User principal) {
        return userService.deletePicture(principal.getUserId());
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe(@AuthenticationPrincipal User principal) {
        userService.delete(principal.getUserId());
    }

    @GetMapping("/me/layoofs")
    public List<LayoofResponseDto> myLayoofs(@AuthenticationPrincipal User principal) {
        return layoofService.listByAuthor(principal);
    }

    @PostMapping("/me/layoofs")
    @ResponseStatus(HttpStatus.CREATED)
    public LayoofResponseDto createLayoof(@AuthenticationPrincipal User principal,
                                          @RequestBody @Valid LayoofRequestDto request) {

        return layoofService.create(request, principal);
    }

    @PutMapping("/me/layoofs/{layoofId}")
    public LayoofResponseDto updateLayoof(@AuthenticationPrincipal User principal,
                                          @PathVariable UUID layoofId,
                                          @RequestBody @Valid LayoofRequestDto request) {

        return layoofService.update(layoofId, request, principal);
    }

    @PostMapping(path = "/me/layoofs/{layoofId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LayoofResponseDto uploadLayoofImage(@AuthenticationPrincipal User principal,
                                               @PathVariable UUID layoofId,
                                               @ValidImage @RequestPart("file") MultipartFile file) {

        return layoofService.updateImage(layoofId, principal, FileUploads.from(file));
    }

    @DeleteMapping("/me/layoofs/{layoofId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLayoof(@AuthenticationPrincipal User principal, @PathVariable UUID layoofId) {
        layoofService.delete(layoofId, principal);
    }

    @GetMapping("/search")
    public List<SearchUserResponseDto> searchUser(@AuthenticationPrincipal User principal,
                                                  @Valid @ModelAttribute SearchUserRequestDto request) {

        return userService.searchUser(request.name(), principal.getUserId());
    }

    @GetMapping("/{userId}")
    public PublicUserResponseDto findById(@PathVariable UUID userId) {
        return userService.findPublicById(userId);
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
