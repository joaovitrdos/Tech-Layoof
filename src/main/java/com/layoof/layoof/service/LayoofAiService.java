package com.layoof.layoof.service;

import com.layoof.layoof.ai.LayoofDraft;
import com.layoof.layoof.ai.LayoofPrompt;
import com.layoof.layoof.exception.AiUnavailableException;
import com.layoof.layoof.infra.config.LayoofAiProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LayoofAiService {

    private static final Logger log = LoggerFactory.getLogger(LayoofAiService.class);

    private static final String DISABLED = "A consulta por IA esta desligada nesta instalacao";
    private static final String NO_MODEL = "Nenhum modelo de IA esta configurado nesta instalacao";
    private static final String FAILURE = "Nao foi possivel consultar a IA agora, tente novamente em instantes";

    private final ObjectProvider<ChatClient> chatClients;
    private final LayoofAiProperties properties;

    public boolean isAvailable() {
        return properties.enabled() && chatClients.getIfAvailable() != null;
    }

    public LayoofDraft research(String target) {
        ChatClient chatClient = requireChatClient();

        try {
            return chatClient.prompt()
                    .user(user -> user.text(LayoofPrompt.RESEARCH).param("target", target))
                    .call()
                    .entity(LayoofDraft.class);
        } catch (Exception ex) {
            log.error("Falha ao pesquisar demissao por IA", ex);
            throw new AiUnavailableException(FAILURE, ex);
        }
    }

    public LayoofDraft extract(String url, String source, String title, String content) {
        ChatClient chatClient = requireChatClient();

        try {
            return chatClient.prompt()
                    .user(user -> user.text(LayoofPrompt.EXTRACTION)
                            .param("url", url)
                            .param("source", blankIfNull(source))
                            .param("title", blankIfNull(title))
                            .param("content", blankIfNull(content)))
                    .call()
                    .entity(LayoofDraft.class);
        } catch (Exception ex) {
            log.error("Falha ao extrair demissao da materia {}", url, ex);
            throw new AiUnavailableException(FAILURE, ex);
        }
    }

    public String summarize(String company, String title, String content) {
        ChatClient chatClient = available();

        if (chatClient == null) {
            return null;
        }

        try {
            return chatClient.prompt()
                    .user(user -> user.text(LayoofPrompt.SUMMARY)
                            .param("company", blankIfNull(company))
                            .param("title", blankIfNull(title))
                            .param("content", blankIfNull(content)))
                    .call()
                    .content();
        } catch (Exception ex) {
            log.warn("Falha ao gerar o resumo por IA: {}", ex.getMessage());
            return null;
        }
    }

    private ChatClient requireChatClient() {
        if (!properties.enabled()) {
            throw new AiUnavailableException(DISABLED);
        }

        ChatClient chatClient = chatClients.getIfAvailable();
        if (chatClient == null) {
            throw new AiUnavailableException(NO_MODEL);
        }
        return chatClient;
    }

    private ChatClient available() {
        return properties.enabled() ? chatClients.getIfAvailable() : null;
    }

    private String blankIfNull(String value) {
        return value == null ? "" : value;
    }
}
