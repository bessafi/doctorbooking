package com.codingshuttle.youtube.hospitalManagement.security;

//package com.clinic.doctorappointment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import java.util.Collections;
import java.util.Enumeration;
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
    private static final String API_KEY_HEADER = "X-API-KEY";



    @PostConstruct
    public void printApiKey() {
        log.info("Configured Botpress API Key: '{}'", secretApiKey);
    }

    /*
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
*/

/*    
@Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.contains("/botpress/")) {
            // Not a botpress request → skip
            filterChain.doFilter(request, response);
            return;
        }

        log.info("--- Botpress API Key Verification ---");
        log.info("Request Path: {}", path);

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("ApiKey ")) {
            log.warn("Missing or invalid Authorization header.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String receivedApiKey = authHeader.substring("ApiKey ".length()).trim();
        log.info("Expected API Key: {}", secretApiKey);
        log.info("Received API Key: {}", receivedApiKey);

        if (secretApiKey.equals(receivedApiKey)) {
            log.info("Access Granted: API keys match.");

            // ✅ Mark as authenticated (no roles needed for Botpress)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("botpress", null, List.of());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } else {
            log.warn("Access Denied: API key mismatch.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }
*/


/*    
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {

    String path = request.getRequestURI();

    // Only secure /api/v1/botpress/**
    if (!path.contains("/botpress/")) {
        filterChain.doFilter(request, response);
        return;
    }

    log.info("--- Botpress API Key Verification ---");
    log.info("Request Path: {}", path);

    String authHeader = request.getHeader("Authorization");
    String apiKeyHeader = request.getHeader("X-API-KEY");

    String receivedApiKey = null;

    if (authHeader != null && authHeader.startsWith("ApiKey ")) {
        receivedApiKey = authHeader.substring("ApiKey ".length()).trim();
    } else if (apiKeyHeader != null) {
        receivedApiKey = apiKeyHeader.trim();
    }

    log.info("Expected API Key: {}", secretApiKey);
    log.info("Received API Key: {}", receivedApiKey);

    if (secretApiKey.equals(receivedApiKey)) {
        log.info("Access Granted: API keys match.");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("botpress", null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    } else {
        log.warn("Access Denied: API key mismatch.");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
*/

@Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info("Full Request URL: {}", request.getRequestURL());
        log.info("Servlet Path: {}", request.getServletPath());
        log.info("Request URI: {}", path);

        // Dump all headers for debugging
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            log.info("--- Incoming Headers ---");
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                log.info("{}: {}", headerName, request.getHeader(headerName));
            }
        }

        // Only apply this filter for botpress endpoints
        if (!(path.contains("/botpress/"))) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info("--- Botpress API Key Verification ---");

        // 1. Try Authorization: ApiKey <key>
        String authHeader = request.getHeader(AUTH_HEADER);
        String receivedApiKey = null;
        if (authHeader != null && authHeader.startsWith(API_KEY_PREFIX)) {
            receivedApiKey = authHeader.substring(API_KEY_PREFIX.length()).trim();
        }

        // 2. Fallback to X-API-KEY
        if (receivedApiKey == null) {
            receivedApiKey = request.getHeader(API_KEY_HEADER);
        }

        log.info("Expected API Key: {}", secretApiKey);
        log.info("Received API Key: {}", receivedApiKey);

        if (secretApiKey.equals(receivedApiKey)) {
            log.info("✅ Access Granted: API keys match.");
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("botpress", null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } else {
            log.warn("❌ Access Denied: API key missing or mismatch.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }
    
}
