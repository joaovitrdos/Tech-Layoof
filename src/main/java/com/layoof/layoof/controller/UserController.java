package com.layoof.layoof.controller;

import com.layoof.layoof.dto.request.UpdateUserRequestDto;
import com.layoof.layoof.dto.response.UserResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponseDto me(@AuthenticationPrincipal User principal) {
        return userService.findById(principal.getUserId());
    }

    @PutMapping("/me")
    public UserResponseDto updateMe(@AuthenticationPrincipal User principal,
                                    @RequestBody @Valid UpdateUserRequestDto request) {
        return userService.updateProfile(principal.getUserId(), request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe(@AuthenticationPrincipal User principal) {
        userService.delete(principal.getUserId());
    }

    @GetMapping("/{userId}")
    public UserResponseDto findById(@PathVariable UUID userId) {
        return userService.findById(userId);
    }

}
