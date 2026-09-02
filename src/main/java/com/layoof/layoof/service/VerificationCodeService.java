package com.layoof.layoof.service;

import com.layoof.layoof.entity.User;
import com.layoof.layoof.entity.VerificationCode;
import com.layoof.layoof.exception.InvalidVerificationCodeException;
import com.layoof.layoof.repository.VerificationCodeRepository;
import com.layoof.layoof.util.EmailNormalizer;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private static final String INVALID = "Codigo de verificacao invalido";
    private static final String EXPIRED = "Codigo de verificacao expirado";

    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository,
                                   PasswordEncoder passwordEncoder) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String createCode(User user) {
        invalidatePendingCodes(user.getEmail());

        String code = generateCode();
        VerificationCode verification = VerificationCode.builder()
                .user(user)
                .codeHash(passwordEncoder.encode(code))
                .expiresAt(LocalDateTime.now().plus(CODE_TTL))
                .build();

        verificationCodeRepository.save(verification);
        return code;
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
                .findAllByUserEmailAndUsedFalse(EmailNormalizer.normalize(email)).stream()
                .filter(candidate -> matches(code, candidate))
                .findFirst()
                .orElseThrow(() -> new InvalidVerificationCodeException(INVALID));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidVerificationCodeException(EXPIRED);
        }
        return verification;
    }

    private boolean matches(String code, VerificationCode candidate) {
        return code != null
                && candidate.getCodeHash() != null
                && passwordEncoder.matches(code, candidate.getCodeHash());
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
