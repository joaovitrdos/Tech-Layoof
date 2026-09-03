package com.layoof.layoof.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.layoof.layoof.dto.request.GoogleAuthRequestDto;
import com.layoof.layoof.dto.request.LoginRequestDto;
import com.layoof.layoof.dto.request.RegisterRequestDto;
import com.layoof.layoof.dto.response.AuthResponseDto;
import com.layoof.layoof.dto.response.RegisterResponseDto;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.enums.AuthProvider;
import com.layoof.layoof.exception.EmailAlreadyInUseException;
import com.layoof.layoof.exception.GoogleAccountAlreadyExistsException;
import com.layoof.layoof.exception.InvalidCredentialsException;
import com.layoof.layoof.exception.InvalidGoogleTokenException;
import com.layoof.layoof.exception.InvalidRegistrationDataException;
import com.layoof.layoof.infra.security.LoginAttemptGuard;
import com.layoof.layoof.infra.security.TokenService;
import com.layoof.layoof.mapper.UserMapper;
import com.layoof.layoof.notification.EmailFactory;
import com.layoof.layoof.notification.EmailRequestedEvent;
import com.layoof.layoof.repository.UserRepository;
import com.layoof.layoof.util.EmailNormalizer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final EmailFactory emailFactory;
    private final ApplicationEventPublisher events;
    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final LoginAttemptGuard loginAttemptGuard;

    private String decoyHash;

    @PostConstruct
    void prepareDecoyHash() {
        decoyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {
        validateRegister(request);

        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException("Ja existe um usuario cadastrado com o email: " + email);
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .authProvider(AuthProvider.LOCAL)
                .build();

        User saved = persistNewUser(user, email);
        events.publishEvent(new EmailRequestedEvent(
                emailFactory.createWelcomeEmail(saved.getEmail(), saved.getName()), saved));

        return userMapper.toRegisterResponse(saved);
    }

    private User persistNewUser(User user, String email) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyInUseException("Ja existe um usuario cadastrado com o email: " + email, ex);
        }
    }

    private void validateRegister(RegisterRequestDto request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidRegistrationDataException("O nome e obrigatorio");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new InvalidRegistrationDataException("O email e obrigatorio");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new InvalidRegistrationDataException("A senha e obrigatoria");
        }
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        String email = normalizeEmail(request.email());
        loginAttemptGuard.assertNotLocked(email);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            burnPasswordComparison(request.password());
            throw failedAttempt(email);
        }
        if (!user.hasLocalPassword()) {
            throw new GoogleAccountAlreadyExistsException(
                    "Esta conta foi criada com o Google. Entre usando o botao 'Entrar com Google'");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw failedAttempt(email);
        }

        loginAttemptGuard.recordSuccess(email);
        return issueToken(user);
    }

    private InvalidCredentialsException failedAttempt(String email) {
        loginAttemptGuard.recordFailure(email);
        return new InvalidCredentialsException("Email ou senha invalidos");
    }

    private void burnPasswordComparison(String rawPassword) {
        passwordEncoder.matches(rawPassword, decoyHash);
    }

    @Transactional
    public AuthResponseDto loginWithGoogle(GoogleAuthRequestDto request) {
        return issueToken(resolveGoogleUser(request));
    }


    private User resolveGoogleUser(GoogleAuthRequestDto request) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.idToken());

        String googleId = requiredClaim(payload.getSubject(),
                "O token do Google nao contem o identificador do usuario");
        String email = normalizeEmail(requiredClaim(payload.getEmail(),
                "O token do Google nao contem um e-mail"));
        requireVerifiedEmail(payload);
        String name = optionalClaim(payload, "name");
        String picture = optionalClaim(payload, "picture");

        return userRepository.findByGoogleId(googleId)
                .map(user -> refreshGoogleProfile(user, name, picture))
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(user -> linkGoogleAccount(user, googleId, picture))
                        .orElseGet(() -> createFromGoogle(googleId, email, name, picture)));
    }

    private void requireVerifiedEmail(GoogleIdToken.Payload payload) {
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new InvalidGoogleTokenException("O e-mail da conta Google nao foi verificado pelo Google");
        }
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }

    private AuthResponseDto issueToken(User user) {
        return AuthResponseDto.bearer(tokenService.generateToken(user), userMapper.toResponse(user));
    }

    private User createFromGoogle(String googleId, String email, String name, String picture) {
        User user = User.builder()
                .name(name != null ? name : email)
                .email(email)
                .picture(picture)
                .googleId(googleId)
                .authProvider(AuthProvider.GOOGLE)
                .build();
        return userRepository.save(user);
    }

    private User linkGoogleAccount(User user, String googleId, String picture) {
        user.setGoogleId(googleId);
        if (picture != null) {
            user.setPicture(picture);
        }
        return userRepository.save(user);
    }

    private User refreshGoogleProfile(User user, String name, String picture) {
        if (name != null) {
            user.setName(name);
        }
        if (picture != null) {
            user.setPicture(picture);
        }
        return userRepository.save(user);
    }

    private String requiredClaim(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new InvalidGoogleTokenException(errorMessage);
        }
        return value;
    }

    private String optionalClaim(GoogleIdToken.Payload payload, String claim) {
        Object value = payload.get(claim);
        return value instanceof String text && !text.isBlank() ? text : null;
    }

    private String normalizeEmail(String email) {
        return EmailNormalizer.normalize(email);
    }
}
