package com.layoof.layoof.service;

import com.layoof.layoof.dto.request.ResetPasswordRequestDto;
import com.layoof.layoof.dto.request.SendEmailRequestDto;
import com.layoof.layoof.dto.request.ValidateCodeRequestDto;
import com.layoof.layoof.dto.response.ResetPasswordResponseDto;
import com.layoof.layoof.dto.response.SendEmailResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.exception.InvalidVerificationCodeException;
import com.layoof.layoof.notification.EmailFactory;
import com.layoof.layoof.repository.UserRepository;
import com.layoof.layoof.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
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

        verificationCodeService.consumeCode(request.code(), email);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return new ResetPasswordResponseDto("Senha atualizada com sucesso");
    }

    private void sendCode(User user) {
        String code = verificationCodeService.createCode(user);
        emailSenderService.sendAsync(emailFactory.createPasswordRecovery(user.getEmail(), code), user);
    }
}
