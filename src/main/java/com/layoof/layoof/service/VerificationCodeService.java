package com.layoof.layoof.service;

import com.layoof.layoof.entity.User;
import com.layoof.layoof.entity.VerificationCode;
import com.layoof.layoof.exception.InvalidVerificationCodeException;
import com.layoof.layoof.repository.VerificationCodeRepository;
import com.layoof.layoof.util.EmailNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificationCodeService {

    static final Duration CODE_TTL = Duration.ofMinutes(15);

    private static final int CODE_BOUND = 1_000_000;
    private static final String CODE_FORMAT = "%06d";

    private final VerificationCodeRepository verificationCodeRepository;
    private final SecureRandom random = new SecureRandom();

    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository) {
        this.verificationCodeRepository = verificationCodeRepository;
    }

    @Transactional
    public String createCode(User user) {
        invalidatePendingCodes(user.getEmail());

        VerificationCode verification = VerificationCode.builder()
                .user(user)
                .code(generateCode())
                .expiresAt(LocalDateTime.now().plus(CODE_TTL))
                .build();

        return verificationCodeRepository.save(verification).getCode();
    }

    @Transactional(readOnly = true)
    public void validateCode(String code, String email) {
        findValidCode(code, email);
    }

    @Transactional
    public void consumeCode(String code, String email) {
        VerificationCode verification = findValidCode(code, email);
        verification.setUsed(true);
        verificationCodeRepository.save(verification);
    }

    private VerificationCode findValidCode(String code, String email) {
        VerificationCode verification = verificationCodeRepository
                .findByCodeAndUserEmailAndUsedFalse(code, EmailNormalizer.normalize(email))
                .orElseThrow(() -> new InvalidVerificationCodeException("Codigo de verificacao invalido"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidVerificationCodeException("Codigo de verificacao expirado");
        }
        return verification;
    }

    private void invalidatePendingCodes(String email) {
        List<VerificationCode> pending = verificationCodeRepository
                .findAllByUserEmailAndUsedFalse(EmailNormalizer.normalize(email));

        if (pending.isEmpty()) {
            return;
        }
        pending.forEach(code -> code.setUsed(true));
        verificationCodeRepository.saveAll(pending);
    }

    private String generateCode() {
        return CODE_FORMAT.formatted(random.nextInt(CODE_BOUND));
    }
}
