package com.layoof.layoof.notification;

import org.springframework.stereotype.Component;

@Component
public class EmailFactory {

    public EmailMessage createWelcomeEmail(String email, String name) {
        return new NewUserEmail(email, name);
    }

    public EmailMessage createLayoffNotification(String email, String details) {
        return new NewLayoofEmail(email, details);
    }

    public EmailMessage createPasswordRecovery(String email, String token) {
        return new PasswordRecoveryEmail(email, token);
    }
}