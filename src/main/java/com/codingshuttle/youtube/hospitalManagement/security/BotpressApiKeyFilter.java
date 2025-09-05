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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
/* 
@Component
@RequiredArgsConstructor
public class BotpressApiKeyFilter extends OncePerRequestFilter {

    // Manually declared logger to fix the compilation error
    private static final Logger log = LoggerFactory.getLogger(BotpressApiKeyFilter.class);

    @Value("${app.botpress.api-key}")
    private String secretApiKey;

    private static final String API_KEY_HEADER = "X-API-KEY";


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // --- START: Added Logging for Verification ---

         String path = request.getServletPath();
        log.info("Filter is processing request for path: {}", path);
        // --- END: Aggressive Logging for Debugging ---

        // Only apply API key validation to the Botpress endpoints
        if (path != null && path.startsWith("/botpress")) {
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
}*/

@Component
@RequiredArgsConstructor
public class BotpressApiKeyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(BotpressApiKeyFilter.class);

    @Value("${app.botpress.api-key}")
    private String secretApiKey;

    // Use the standard Authorization header
    private static final String AUTH_HEADER = "Authorization";
    private static final String API_KEY_PREFIX = "ApiKey ";



    @PostConstruct
    public void printApiKey() {
        log.info("Configured Botpress API Key: '{}'", secretApiKey);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        // Only apply this filter to the Botpress endpoints
        if (path != null && path.startsWith("/botpress")) {
            String authHeader = request.getHeader(AUTH_HEADER);
            String incomingApiKey = null;

            if (authHeader != null && authHeader.startsWith(API_KEY_PREFIX)) {
                incomingApiKey = authHeader.substring(API_KEY_PREFIX.length());
            }

            log.info("--- Botpress API Key Verification ---");
            log.info("Request Path: {}", path);
            log.info("Expected API Key: {}", secretApiKey);
            log.info("Received API Key: {}", incomingApiKey);

            if (secretApiKey.equals(incomingApiKey)) {
                log.info("Access Granted: API keys match.");
                filterChain.doFilter(request, response);
            } else {
                log.warn("Access Denied: API key is missing or invalid.");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid API Key");
                return;
            }
        } else {
            // If it's not a botpress URL, continue to the next filter
            filterChain.doFilter(request, response);
        }
    }
}

