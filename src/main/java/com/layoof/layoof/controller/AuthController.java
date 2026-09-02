package com.layoof.layoof.controller;

import com.layoof.layoof.dto.request.GoogleAuthRequestDto;
import com.layoof.layoof.dto.request.LoginRequestDto;
import com.layoof.layoof.dto.request.RegisterRequestDto;
import com.layoof.layoof.dto.request.ResetPasswordRequestDto;
import com.layoof.layoof.dto.request.SendEmailRequestDto;
import com.layoof.layoof.dto.request.ValidateCodeRequestDto;
import com.layoof.layoof.dto.response.AuthResponseDto;
import com.layoof.layoof.dto.response.RegisterResponseDto;
import com.layoof.layoof.dto.response.ResetPasswordResponseDto;
import com.layoof.layoof.dto.response.SendEmailResponseDto;
import com.layoof.layoof.service.AuthService;
import com.layoof.layoof.service.PasswordRecoveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponseDto register(@RequestBody @Valid RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody @Valid LoginRequestDto request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public AuthResponseDto loginWithGoogle(@RequestBody @Valid GoogleAuthRequestDto request) {
        return authService.loginWithGoogle(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authService.logout(authorization);
    }

    @PostMapping("/password/forgot")
    public SendEmailResponseDto forgotPassword(@RequestBody @Valid SendEmailRequestDto request) {
        return passwordRecoveryService.sendRecoveryCode(request);
    }

    @PostMapping("/password/validate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validateRecoveryCode(@RequestBody @Valid ValidateCodeRequestDto request) {
        passwordRecoveryService.validateCode(request);
    }

    @PostMapping("/password/reset")
    public ResetPasswordResponseDto resetPassword(@RequestBody @Valid ResetPasswordRequestDto request) {
        return passwordRecoveryService.resetPassword(request);
    }
}
