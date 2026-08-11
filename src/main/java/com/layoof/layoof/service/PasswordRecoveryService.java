package com.layoof.layoof.service;

import com.layoof.layoof.dto.request.ChangePasswordRequestDto;
import com.layoof.layoof.dto.request.ResetPasswordRequestDto;
import com.layoof.layoof.dto.request.SendEmailRequestDto;
import com.layoof.layoof.dto.request.ValidateCodeRequestDto;
import com.layoof.layoof.dto.response.ResetPasswordResponseDto;
import com.layoof.layoof.dto.response.SendEmailResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.enums.AuthProvider;
import com.layoof.layoof.exception.InvalidVerificationCodeException;
import com.layoof.layoof.notification.EmailFactory;
import com.layoof.layoof.repository.UserRepository;
import com.layoof.layoof.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private final UserRepository userRepository;
    private final VerificationCodeService verificationCodeService;
    private final EmailFactory emailFactory;
    private final EmailSenderService emailSenderService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SendEmailResponseDto sendRecoveryCode(SendEmailRequestDto request) {
        userRepository.findByEmail(EmailNormalizer.normalize(request.email()))
                .filter(User::hasLocalPassword)
                .ifPresent(this::sendCode);

        return new SendEmailResponseDto(
                "Se o e-mail estiver cadastrado, enviaremos um codigo de verificacao");
    }

    @Transactional(readOnly = true)
    public void validateCode(ValidateCodeRequestDto request) {
        verificationCodeService.validateCode(request.code(), request.email());
    }

    @Transactional
    public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto request) {
        String email = EmailNormalizer.normalize(request.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidVerificationCodeException(InvalidVerificationCodeException.INVALID_CODE));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new IllegalStateException("Usuários cadastrados via Google devem fazer login com a conta Google e não possuem senha para redefinir");
        }

        verificationCodeService.consumeCode(request.code(), email);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return new ResetPasswordResponseDto("Senha atualizada com sucesso");
    }

    @Transactional
    public ResetPasswordResponseDto changePassword(User user, ChangePasswordRequestDto request) {
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new IllegalStateException("Contas do Google não possuem senha para alterar");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("A senha atual informada está incorreta");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return new ResetPasswordResponseDto("Senha alterada com sucesso");
    }

    private void sendCode(User user) {
        String code = verificationCodeService.createCode(user);
        emailSenderService.sendAsync(emailFactory.createPasswordRecovery(user.getEmail(), code), user);
    }
}
