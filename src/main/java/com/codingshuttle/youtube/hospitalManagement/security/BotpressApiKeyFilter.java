package com.codingshuttle.youtube.hospitalManagement.security;

//package com.clinic.doctorappointment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class BotpressApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.botpress.api-key}")
    private String secretApiKey;

    private static final String API_KEY_HEADER = "X-API-KEY";


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // --- START: Added Logging for Verification ---

        // Only apply this filter to the Botpress endpoints
        if (request.getRequestURI().startsWith("/api/v1/botpress")) {
            String incomingApiKey = request.getHeader("X-API-KEY");

            log.info("--- Botpress API Key Verification ---");
            log.info("Request URI: {}", request.getRequestURI());
            log.info("Expected API Key: {}", secretApiKey);
            log.info("Received API Key: {}", incomingApiKey);

            if (secretApiKey.equals(incomingApiKey)) {
                log.info("Access Granted: API keys match.");
                filterChain.doFilter(request, response);
            } else {
                log.warn("Access Denied: API keys DO NOT match.");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid API Key");
                return; // Stop the filter chain
            }
        } else {
            // If it's not a botpress URL, just continue to the next filter
            filterChain.doFilter(request, response);
        }
        // --- END: Added Logging for Verification ---
    }
}
