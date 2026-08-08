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
import com.layoof.layoof.infra.security.TokenService;
import com.layoof.layoof.mapper.UserMapper;
import com.layoof.layoof.notification.EmailFactory;
import com.layoof.layoof.repository.UserRepository;
import com.layoof.layoof.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final EmailFactory emailFactory;
    private final EmailSenderService emailSenderService;
    private final UserMapper userMapper;
    private final TokenService tokenService;

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {
        validateRegister(request);

        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException(email);
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .authProvider(AuthProvider.LOCAL)
                .build();

        User saved = persistNewUser(user, email);
        emailSenderService.sendAsync(emailFactory.createWelcomeEmail(saved.getEmail(), saved.getName()), saved);

        return userMapper.toRegisterResponse(saved);
    }

    private User persistNewUser(User user, String email) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyInUseException(email, ex);
        }
    }

    private void validateRegister(RegisterRequestDto request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidRegistrationDataException(InvalidRegistrationDataException.MISSING_NAME);
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new InvalidRegistrationDataException(InvalidRegistrationDataException.MISSING_EMAIL);
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new InvalidRegistrationDataException(InvalidRegistrationDataException.MISSING_PASSWORD);
        }
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.hasLocalPassword()) {
            throw new GoogleAccountAlreadyExistsException();
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return issueToken(user);
    }

    @Transactional
    public AuthResponseDto loginWithGoogle(GoogleAuthRequestDto request) {
        return issueToken(resolveGoogleUser(request));
    }

    private User resolveGoogleUser(GoogleAuthRequestDto request) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.idToken());

        String googleId = requiredClaim(payload.getSubject(), InvalidGoogleTokenException.MISSING_SUBJECT);
        String email = normalizeEmail(requiredClaim(payload.getEmail(), InvalidGoogleTokenException.MISSING_EMAIL));
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
            throw new InvalidGoogleTokenException(InvalidGoogleTokenException.EMAIL_NOT_VERIFIED);
        }
    }

    private AuthResponseDto issueToken(User user) {
        return AuthResponseDto.bearer(
                tokenService.generateToken(user),
                userMapper.toResponse(user));
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
