package com.layoof.layoof.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.layoof.layoof.exception.InvalidGoogleTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Component
public class GoogleTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifier.class);

    private final GoogleIdTokenVerifier verifier;
    private final boolean configured;

    public GoogleTokenVerifier(@Value("${layoof.google.client-id:}") String clientId) {
        this.configured = StringUtils.hasText(clientId);
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(List.of(clientId))
                .build();

        if (!configured) {
            log.warn("layoof.google.client-id nao configurado: o login com Google ficara indisponivel");
        }
    }

    public GoogleIdToken.Payload verify(String idToken) {
        if (!StringUtils.hasText(idToken)) {
            throw new InvalidGoogleTokenException("Token do Google nao informado");
        }
        if (!configured) {
            throw new InvalidGoogleTokenException("Nao foi possivel validar o token do Google");
        }

        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new InvalidGoogleTokenException("Token do Google invalido ou expirado");
            }
            return token.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidGoogleTokenException("Nao foi possivel validar o token do Google", e);
        }
    }
}
