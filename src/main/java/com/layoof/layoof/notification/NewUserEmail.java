package com.layoof.layoof.notification;

import com.layoof.layoof.enums.TypeEmail;

public class NewUserEmail implements EmailMessage {

    private static final String TITLE = "Bem-vindo ao Tech Layoof";
    private static final String PREVIEW = "Sua conta foi criada com sucesso";

    private static final String CONTENT = """
            <h2 style="margin:0 0 20px; color:#111827; font-size:24px;">
                Olá, {{name}}! 👋
            </h2>

            <p style="margin:0 0 16px; font-size:16px; line-height:1.6; color:#4b5563;">
                É um prazer ter você conosco.
            </p>

            <p style="margin:0 0 24px; font-size:16px; line-height:1.6; color:#4b5563;">
                Sua conta no <strong>Tech Layoof</strong> foi criada com sucesso.
                Agora você pode acompanhar informações e análises sobre o mercado de tecnologia.
            </p>

            <table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"
                   style="background-color:#f5f3ff; border-left:4px solid #6366f1; border-radius:6px;">
                <tr>
                    <td style="padding:18px;">
                        <p style="margin:0; font-size:15px; line-height:1.5; color:#4338ca;">
                            Sua jornada começa agora. Explore o Tech Layoof e fique
                            por dentro do mercado de tecnologia.
                        </p>
                    </td>
                </tr>
            </table>

            <p style="margin:30px 0 0; font-size:16px; line-height:1.6; color:#4b5563;">
                Esperamos que nossa plataforma seja útil para você.
            </p>
            """;

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
        return "Bem-vindo ao Tech Layoof!";
    }

    @Override
    public String getBody() {
        return EmailLayout.render(TITLE, PREVIEW, CONTENT.replace("{{name}}", EmailLayout.escape(name)));
    }

    @Override
    public TypeEmail getType() {
        return TypeEmail.NEW_USER;
    }
}
