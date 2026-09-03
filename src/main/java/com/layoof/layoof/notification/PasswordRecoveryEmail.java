package com.layoof.layoof.notification;

import com.layoof.layoof.enums.TypeEmail;

public class PasswordRecoveryEmail implements EmailMessage {

    private static final String TITLE = "Recuperação de senha";
    private static final String PREVIEW = "Seu código de verificação expira em 15 minutos";

    private static final String CONTENT = """
            <h2 style="margin:0 0 20px; color:#111827; font-size:24px;">
                Recuperação de senha
            </h2>

            <p style="margin:0 0 16px; font-size:16px; line-height:1.6; color:#4b5563;">
                Recebemos uma solicitação para redefinir a senha da sua conta
                no <strong>Tech Layoof</strong>.
            </p>

            <p style="margin:25px 0 12px; font-size:15px; color:#6b7280; text-align:center;">
                Seu código de verificação é:
            </p>

            <table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">
                <tr>
                    <td align="center" style="padding:10px 0 25px;">
                        <div style="display:inline-block; padding:18px 35px;
                                    background-color:#f5f3ff;
                                    border:2px solid #6366f1;
                                    border-radius:10px;
                                    font-size:32px;
                                    font-weight:bold;
                                    letter-spacing:8px;
                                    color:#4338ca;">
                            {{code}}
                        </div>
                    </td>
                </tr>
            </table>

            <table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"
                   style="background-color:#fff7ed; border-left:4px solid #f97316; border-radius:6px;">
                <tr>
                    <td style="padding:16px;">
                        <p style="margin:0; font-size:14px; line-height:1.5; color:#9a3412;">
                            <strong>Importante:</strong> este código expira em
                            <strong>15 minutos</strong> e só pode ser utilizado uma vez.
                        </p>
                    </td>
                </tr>
            </table>

            <p style="margin:25px 0 0; font-size:15px; line-height:1.6; color:#6b7280;">
                Se você não solicitou a recuperação da sua senha, ignore este
                e-mail. Sua conta continuará protegida.
            </p>
            """;

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
        return "Recuperação de senha — Tech Layoof";
    }

    @Override
    public String getBody() {
        return EmailLayout.render(TITLE, PREVIEW, CONTENT.replace("{{code}}", EmailLayout.escape(code)));
    }

    @Override
    public TypeEmail getType() {
        return TypeEmail.REFRESH_PASSWORD;
    }
}
