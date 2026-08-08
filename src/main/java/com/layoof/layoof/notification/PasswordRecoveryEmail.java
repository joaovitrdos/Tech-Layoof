package com.layoof.layoof.notification;

import com.layoof.layoof.enums.TypeEmail;

public class PasswordRecoveryEmail implements EmailMessage {
    private final String email;
    private final String code;

    public PasswordRecoveryEmail(String email, String code) {
        this.email = email;
        this.code = code;
    }

    @Override
    public String getRecipient() {
        return email;
    }

    @Override
    public String getSubject() {
        return "Recuperacao de senha";
    }

    @Override
    public String getBody() {
        return """
                Recebemos um pedido para redefinir a sua senha.

                Seu codigo de verificacao e: %s
                Ele expira em 15 minutos e so pode ser usado uma vez.

                Se voce nao pediu a troca de senha, ignore este e-mail.""".formatted(this.code);
    }

    @Override
    public TypeEmail getType() {
        return TypeEmail.REFRESH_PASSWORD;
    }
}
