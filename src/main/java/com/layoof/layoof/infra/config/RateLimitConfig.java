package com.layoof.layoof.infra.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    private static final Duration BUCKET_RETENTION = Duration.ofHours(1);

    @Bean(destroyMethod = "shutdown")
    public RedisClient rateLimitRedisClient(DataRedisProperties properties) {
        RedisURI.Builder uri = RedisURI.builder()
                .withHost(properties.getHost())
                .withPort(properties.getPort())
                .withTimeout(properties.getTimeout());

        if (StringUtils.hasText(properties.getPassword())) {
            uri.withPassword(properties.getPassword().toCharArray());
        }
        return RedisClient.create(uri.build());
    }

    @Bean(destroyMethod = "close")
    @Lazy
    public StatefulRedisConnection<String, byte[]> rateLimitConnection(RedisClient rateLimitRedisClient) {
        return rateLimitRedisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
    }

    @Bean
    @Lazy
    public ProxyManager<String> rateLimitProxyManager(StatefulRedisConnection<String, byte[]> rateLimitConnection) {
        return Bucket4jLettuce.casBasedBuilder(rateLimitConnection)
                .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(BUCKET_RETENTION))
                .build();
    }
}
