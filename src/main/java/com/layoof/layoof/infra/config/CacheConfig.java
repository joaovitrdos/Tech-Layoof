package com.layoof.layoof.infra.config;

import com.layoof.layoof.dto.response.LayoofResponseDto;
import com.layoof.layoof.dto.response.SearchLayoofResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.TypeFactory;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    public static final String LAYOOF_COUNT = "layoofCount";
    public static final String USER_COUNT = "userCount";
    public static final String SOURCE_COUNT = "sourceCount";
    public static final String LAYOOF_BY_ID = "layoofById";
    public static final String LAYOOF_SEARCH = "layoofSearch";

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    private static final String KEY_PREFIX = "layoof:cache:";
    private static final Duration COUNT_TTL = Duration.ofMinutes(5);
    private static final Duration LOOKUP_TTL = Duration.ofMinutes(10);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        TypeFactory types = TypeFactory.createDefaultInstance();

        JavaType countType = types.constructType(Long.class);
        JavaType layoofType = types.constructType(LayoofResponseDto.class);
        JavaType searchType = types.constructCollectionType(List.class, SearchLayoofResponseDto.class);

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration(LAYOOF_COUNT, configuration(COUNT_TTL, objectMapper, countType))
                .withCacheConfiguration(USER_COUNT, configuration(COUNT_TTL, objectMapper, countType))
                .withCacheConfiguration(SOURCE_COUNT, configuration(COUNT_TTL, objectMapper, countType))
                .withCacheConfiguration(LAYOOF_BY_ID, configuration(LOOKUP_TTL, objectMapper, layoofType))
                .withCacheConfiguration(LAYOOF_SEARCH, configuration(LOOKUP_TTL, objectMapper, searchType))
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new FallThroughErrorHandler();
    }

    private RedisCacheConfiguration configuration(Duration ttl, ObjectMapper objectMapper, JavaType type) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .prefixCacheNameWith(KEY_PREFIX)
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(objectMapper, type)));
    }

    private static final class FallThroughErrorHandler implements CacheErrorHandler {

        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Cache '{}' indisponivel na leitura, consultando o banco: {}",
                    cache.getName(), exception.getMessage());
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            log.warn("Cache '{}' indisponivel na escrita: {}", cache.getName(), exception.getMessage());
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Cache '{}' indisponivel na invalidacao: {}", cache.getName(), exception.getMessage());
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            log.warn("Cache '{}' indisponivel na limpeza: {}", cache.getName(), exception.getMessage());
        }
    }
}
