package com.layoof.layoof.infra.config;

import com.layoof.layoof.ai.LayoofPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({LayoofAiProperties.class, LayoofIngestionProperties.class})
public class AiConfig {

    @Bean
    public ChatClient layoofChatClient(ObjectProvider<ChatClient.Builder> builders) {
        ChatClient.Builder builder = builders.getIfAvailable();

        return builder == null ? null : builder.defaultSystem(LayoofPrompt.STANDARD).build();
    }
}
