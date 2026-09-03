package com.layoof.layoof.notification;

import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;

final class EmailLayout {

    private static final String TEMPLATE = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="color-scheme" content="light">
                <meta name="supported-color-schemes" content="light">
                <title>{{title}}</title>
            </head>
            <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, Helvetica, sans-serif; color:#1f2937;">

            <div class="preheader" style="display:none; max-height:0; overflow:hidden; opacity:0; color:transparent;">{{preview}}</div>

            <table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"
                   style="background-color:#f4f6f8; padding:40px 20px;">
                <tr>
                    <td align="center">

                        <table width="600" cellpadding="0" cellspacing="0" border="0" role="presentation"
                               style="max-width:600px; width:100%; background-color:#ffffff; border-radius:12px; overflow:hidden;">

                            <tr>
                                <td style="background-color:#111827; padding:32px; text-align:center;">
                                    <h1 style="margin:0; color:#ffffff; font-size:28px;">
                                        Tech <span style="color:#6366f1;">Layoof</span>
                                    </h1>
                                </td>
                            </tr>

                            <tr>
                                <td style="padding:40px 35px;">
            {{content}}
                                </td>
                            </tr>

                            <tr>
                                <td style="background-color:#f9fafb; padding:24px 35px; text-align:center; border-top:1px solid #e5e7eb;">
                                    <p style="margin:0 0 8px; font-size:14px; color:#6b7280;">
                                        Atenciosamente,
                                    </p>
                                    <p style="margin:0; font-size:14px; font-weight:bold; color:#111827;">
                                        Equipe Tech Layoof
                                    </p>
                                    <p style="margin:12px 0 0; font-size:12px; color:#9ca3af;">
                                        Este é um e-mail automático. Por favor, não responda.
                                    </p>
                                </td>
                            </tr>

                        </table>

                    </td>
                </tr>
            </table>

            </body>
            </html>
            """;

    private EmailLayout() {
    }

    static String render(String title, String preview, String content) {
        return TEMPLATE
                .replace("{{title}}", escape(title))
                .replace("{{preview}}", escape(preview))
                .replace("{{content}}", content);
    }

    static String escape(String value) {
        return value == null ? "" : HtmlUtils.htmlEscape(value, StandardCharsets.UTF_8.name());
    }
}
