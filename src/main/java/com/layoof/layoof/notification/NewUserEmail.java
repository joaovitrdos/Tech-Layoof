package com.layoof.layoof.notification;

import com.layoof.layoof.enums.TypeEmail;

public class NewUserEmail implements EmailMessage {
    private final String email;
    private final String name;

    public NewUserEmail(String email, String name) {
        this.email = email;
        this.name = name;
    }

    @Override
    public String getRecipient() {
        return email;
    }

    @Override
    public String getSubject() {
        return "Seja Bem Vindo ao Tech Layoof.";
    }

    @Override
    public String getBody() {
        return "Olá %s, sua conta foi criada com sucesso.".formatted(this.name);
    }

    @Override
    public TypeEmail getType() {
        return TypeEmail.NEW_USER;
    }
}
