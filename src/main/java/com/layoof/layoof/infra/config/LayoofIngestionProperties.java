package com.layoof.layoof.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "layoof.ai.ingestion")
public record LayoofIngestionProperties(boolean enabled,
                                        String cron,
                                        String zone,
                                        Duration lookback,
                                        Duration timeout,
                                        List<String> terms,
                                        int quantity) {

    private static final String DEFAULT_CRON = "0 0 8,16 * * *";
    private static final String DEFAULT_ZONE = "America/Sao_Paulo";
    private static final Duration DEFAULT_LOOKBACK = Duration.ofDays(2);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);
    private static final List<String> DEFAULT_TERMS = List.of("tech layoffs", "demissoes tecnologia");
    private static final int DEFAULT_QUANTITY = 4;

    public LayoofIngestionProperties {
        cron = cron == null || cron.isBlank() ? DEFAULT_CRON : cron;
        zone = zone == null || zone.isBlank() ? DEFAULT_ZONE : zone;
        lookback = lookback == null ? DEFAULT_LOOKBACK : lookback;
        timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
        terms = terms == null || terms.isEmpty() ? DEFAULT_TERMS : List.copyOf(terms);
        quantity = quantity <= 0 ? DEFAULT_QUANTITY : quantity;
    }

    public long lookbackDays() {
        return Math.max(1, lookback.toDays());
    }
}
