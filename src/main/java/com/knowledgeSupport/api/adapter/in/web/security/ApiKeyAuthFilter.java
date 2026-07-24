package com.knowledgeSupport.api.adapter.in.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Validates the API key sent in the {@value #HEADER_NAME} header. Doesn't authenticate any
 * user (there's no login) — it only confirms that the caller knows the secret shared via .env.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-API-KEY";

    private final String expectedApiKey;

    public ApiKeyAuthFilter(@Value("${security.api-key}") String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String providedApiKey = request.getHeader(HEADER_NAME);

        if (providedApiKey != null && constantTimeEquals(providedApiKey, expectedApiKey)) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    "api-client", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Comparação em tempo constante: {@code String.equals} retorna no primeiro byte diferente,
     * vazando o prefixo correto por timing. {@code MessageDigest.isEqual} não faz short-circuit
     * revelador e trata tamanhos diferentes com segurança.
     */
    private static boolean constantTimeEquals(String provided, String expected) {
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }
}
