package com.layoof.layoof.notification;

import com.layoof.layoof.enums.TypeEmail;

public class NewLayoofEmail implements EmailMessage {

    private static final String TITLE = "Nova demissão registrada";
    private static final String PREVIEW = "Uma nova demissão acaba de entrar no Tech Layoof";

    private static final String CONTENT = """
            <h2 style="margin:0 0 20px; color:#111827; font-size:24px;">
                Nova demissão registrada
            </h2>

            <p style="margin:0 0 24px; font-size:16px; line-height:1.6; color:#4b5563;">
                Acabou de entrar uma nova demissão no <strong>Tech Layoof</strong>.
            </p>

            <table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"
                   style="background-color:#f5f3ff; border-left:4px solid #6366f1; border-radius:6px;">
                <tr>
                    <td style="padding:18px;">
                        <p style="margin:0; font-size:15px; line-height:1.5; color:#4338ca;">
                            {{details}}
                        </p>
                    </td>
                </tr>
            </table>

            <p style="margin:30px 0 0; font-size:16px; line-height:1.6; color:#4b5563;">
                Acesse a plataforma para ver os detalhes e as fontes da notícia.
            </p>
            """;

    private final String email;
    private final String layoffDetails;

    public NewLayoofEmail(String email, String layoffDetails) {
        this.email = email;
        this.layoffDetails = layoffDetails;
    }

    @Override
    public String getRecipient() {
        return email;
    }

    @Override
    public String getSubject() {
        return "Nova demissão registrada no Tech Layoof";
    }

    @Override
    public String getBody() {
        return EmailLayout.render(TITLE, PREVIEW,
                CONTENT.replace("{{details}}", EmailLayout.escape(layoffDetails)));
    }

    @Override
    public TypeEmail getType() {
        return TypeEmail.NEW_LAYOOF;
    }
}
