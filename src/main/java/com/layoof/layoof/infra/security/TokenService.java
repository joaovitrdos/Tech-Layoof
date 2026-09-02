package com.layoof.layoof.infra.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.layoof.layoof.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class TokenService {

    private static final Duration TOKEN_TTL = Duration.ofDays(7);

    private static final String ISSUER = "auth-api";

    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user, UUID jti) {
        try {
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getEmail())
                    .withJWTId(jti.toString())
                    .withExpiresAt(genExpirationDate())
                    .sign(Algorithm.HMAC256(secret));
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error ao gerar token", exception);
        }
    }

    public UUID sessionIdFrom(String token) {
        DecodedJWT decoded = decode(token);

        if (decoded == null || decoded.getId() == null) {
            return null;
        }

        try {
            return UUID.fromString(decoded.getId());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public UUID sessionIdFromHeader(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }
        return sessionIdFrom(authorizationHeader.substring(BEARER_PREFIX.length()).strip());
    }

    private DecodedJWT decode(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token);
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    private Instant genExpirationDate() {
        return Instant.now().plus(TOKEN_TTL);
    }
}
