package com.layoof.layoof.infra.security;

import com.layoof.layoof.exception.ProblemDetailWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class PayloadSizeFilter extends OncePerRequestFilter {

    private static final Set<String> BODYLESS_METHODS = Set.of("GET", "HEAD", "OPTIONS", "DELETE");

    private static final String TOO_LARGE =
            "O corpo da requisicao excede o tamanho permitido";
    private static final String UNSUPPORTED =
            "Envio de arquivos nao e aceito nesta rota";

    private final long maxBytes;
    private final long maxUploadBytes;
    private final List<PathPattern> uploadPaths;
    private final ProblemDetailWriter problemDetailWriter;

    public PayloadSizeFilter(@Value("${layoof.security.max-payload-size}") DataSize maxPayloadSize,
                             @Value("${layoof.security.max-upload-size}") DataSize maxUploadSize,
                             @Value("${layoof.security.upload-paths}") List<String> uploadPaths,
                             ProblemDetailWriter problemDetailWriter) {

        this.maxBytes = maxPayloadSize.toBytes();
        this.maxUploadBytes = maxUploadSize.toBytes();
        this.uploadPaths = uploadPaths.stream()
                .map(PathPatternParser.defaultInstance::parse)
                .toList();
        this.problemDetailWriter = problemDetailWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        boolean multipart = isMultipart(request);

        if (multipart && !isUploadPath(request)) {
            problemDetailWriter.write(request, response, HttpStatus.UNSUPPORTED_MEDIA_TYPE, UNSUPPORTED);
            return;
        }
        if (request.getContentLengthLong() > (multipart ? maxUploadBytes : maxBytes)) {
            problemDetailWriter.write(request, response, HttpStatus.PAYLOAD_TOO_LARGE, TOO_LARGE);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return BODYLESS_METHODS.contains(request.getMethod());
    }

    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null
                && contentType.toLowerCase(Locale.ROOT).startsWith(MediaType.MULTIPART_FORM_DATA_VALUE);
    }

    private boolean isUploadPath(HttpServletRequest request) {
        PathContainer path = PathContainer.parsePath(request.getRequestURI());
        return uploadPaths.stream().anyMatch(pattern -> pattern.matches(path));
    }
}
