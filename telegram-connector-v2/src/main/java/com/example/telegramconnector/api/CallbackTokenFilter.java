package com.example.telegramconnector.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class CallbackTokenFilter extends OncePerRequestFilter {

    private static final String RESPONSE_PATH = "/api/v1/responses";

    private final byte[] expectedCallbackToken;

    public CallbackTokenFilter(@Value("${telegram-connector.callback-token}") String callbackToken) {
        if (callbackToken == null || callbackToken.isBlank()) {
            throw new IllegalArgumentException("telegram-connector.callback-token must not be blank");
        }
        this.expectedCallbackToken = callbackToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod())
                || !RESPONSE_PATH.equals(withoutMatrixParameters(request));
    }

    private String withoutMatrixParameters(HttpServletRequest request) {
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        return requestPath.replaceAll(";[^/]*", "");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String callbackToken = request.getHeader("X-Connector-Token");
        if (callbackToken == null || !MessageDigest.isEqual(
                expectedCallbackToken,
                callbackToken.getBytes(StandardCharsets.UTF_8)
        )) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
