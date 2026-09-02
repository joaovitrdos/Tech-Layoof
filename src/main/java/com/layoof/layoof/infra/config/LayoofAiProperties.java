package com.layoof.layoof.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "layoof.ai")
public record LayoofAiProperties(boolean enabled) {
}
