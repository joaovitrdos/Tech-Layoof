package com.layoof.layoof.infra.config;

import com.layoof.layoof.exception.ProblemDetailWriter;
import com.layoof.layoof.infra.security.PayloadSizeFilter;
import com.layoof.layoof.infra.security.RateLimitFilter;
import com.layoof.layoof.infra.security.SecurityFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({LayoofSecurityProperties.class, RateLimitProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    private static final int BCRYPT_STRENGTH = 12;
    private static final int HSTS_MAX_AGE_SECONDS = 31_536_000;

    private static final String[] DOCS_PATHS = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**"};

    private static final String CSP_API =
            "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'";

    private static final String CSP_DOCS =
            "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; "
                    + "script-src 'self' 'unsafe-inline'; frame-ancestors 'none'; base-uri 'self'; "
                    + "form-action 'self'";

    private static final String PERMISSIONS_POLICY =
            "accelerometer=(), camera=(), geolocation=(), gyroscope=(), magnetometer=(), "
                    + "microphone=(), payment=(), usb=(), interest-cohort=()";

    private static final List<String> ALLOWED_METHODS =
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    private static final List<String> ALLOWED_HEADERS =
            List.of("Authorization", "Content-Type", "Accept");

    private static final long CORS_MAX_AGE_SECONDS = 3600;

    private final SecurityFilter securityFilter;
    private final ProblemDetailWriter problemDetailWriter;
    private final LayoofSecurityProperties properties;

    @Bean
    public FilterRegistrationBean<SecurityFilter> securityFilterRegistration(SecurityFilter filter) {
        FilterRegistrationBean<SecurityFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter filter) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<PayloadSizeFilter> payloadSizeFilterRegistration(PayloadSizeFilter filter) {
        FilterRegistrationBean<PayloadSizeFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(this::hardenHeaders)
                .authorizeHttpRequests(this::authorizeRequests)
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void authorizeRequests(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {

        if (properties.docsEnabled()) {
            registry.requestMatchers(DOCS_PATHS).permitAll();
        } else {
            registry.requestMatchers(DOCS_PATHS).denyAll();
        }

        registry
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/google").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/password/forgot").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/password/validate").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/password/reset").permitAll()
                .anyRequest().authenticated();
    }

    private void hardenHeaders(HeadersConfigurer<HttpSecurity> headers) {
        headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                        properties.docsEnabled() ? CSP_DOCS : CSP_API))
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(Customizer.withDefaults())
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.NO_REFERRER))
                .permissionsPolicyHeader(permissions -> permissions.policy(PERMISSIONS_POLICY))
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(HSTS_MAX_AGE_SECONDS));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.allowedOrigins());
        configuration.setAllowedMethods(ALLOWED_METHODS);
        configuration.setAllowedHeaders(ALLOWED_HEADERS);
        configuration.setExposedHeaders(List.of("Retry-After"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(CORS_MAX_AGE_SECONDS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> problemDetailWriter.write(
                request, response, HttpStatus.UNAUTHORIZED, "Token ausente ou invalido");
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, deniedException) -> problemDetailWriter.write(
                request, response, HttpStatus.FORBIDDEN, "Voce nao tem permissao para acessar este recurso");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }
}
