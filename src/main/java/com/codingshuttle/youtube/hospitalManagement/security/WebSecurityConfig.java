package com.codingshuttle.youtube.hospitalManagement.security;

//package com.clinic.doctorappointment.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.http.HttpMethod; 
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.web.access.intercept.AuthorizationFilter;



import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import com.codingshuttle.youtube.hospitalManagement.security.OAuth2AuthenticationSuccessHandler;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final BotpressApiKeyFilter botpressApiKeyFilter;

    @Value("${app.oauth.authorized-redirect-uri}")
    private String authorizedRedirectUri;

    /*
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()               
                        .requestMatchers("/auth/**", "/oauth2/**", "/login/**").permitAll()
                       // .requestMatchers("/botpress/**").permitAll() // Security handled by BotpressApiKeyFilter
                       // .requestMatchers("/api/v1/botpress/**").permitAll() // match real path
                        .requestMatchers("/api/v1/botpress/**").authenticated()               
                        .requestMatchers("/botpress/health-check").permitAll()                
                        .requestMatchers("/doctors/**", "/calendar/**").hasRole("DOCTOR")
                        .requestMatchers("/patients/**").hasRole("PATIENT")
                        //.requestMatchers("/availability/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
               
                // --- THIS IS THE FIX ---
                // We run our API key filter BEFORE the main AuthorizationFilter.
                //.addFilterBefore(botpressApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                //.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                 //to test the  fix cors error 
                 .addFilterBefore(botpressApiKeyFilter, AuthorizationFilter.class)
                 .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


                
        return http.build();
    }
*/

 @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- FIX #1: This is the most critical fix for CORS ---
                        // It allows all OPTIONS preflight requests to pass through security.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Public endpoints
                        .requestMatchers("/auth/**", "/oauth2/**", "/login/**").permitAll()
                        .requestMatchers("/botpress/**").permitAll()

                        // Secured endpoints
                        .requestMatchers("/doctors/**").hasRole("DOCTOR")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                // --- FIX #2: This ensures custom filters run at the correct time ---
                .addFilterBefore(botpressApiKeyFilter, AuthorizationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }





    
/*
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // configuration.setAllowedOrigins(List.of("http://localhost:5173", authorizedRedirectUri)); // Add your frontend URL 
        // configuration.setAllowedOrigins(List.of("*", authorizedRedirectUri)); // Add your frontend URL
        configuration.setAllowedOrigins(List.of("http://localhost:5173", authorizedRedirectUri));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-API-KEY"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
*/
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowCredentials(true);

    // DEBUG MODE: allow everything
    //configuration.setAllowedOriginPatterns(List.of("http://localhost:5173"));
    //configuration.setAllowedOrigins(List.of("http://localhost:5173"));
    configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "https://doctorbooking-production.up.railway.app"));


    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-API-KEY", "Accept", "Origin"));
    configuration.setExposedHeaders(List.of("Location")); // optional

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
    
    /**
     * Creates a custom JwtDecoder that allows for a 5-minute clock skew to prevent
     * errors due to time differences between the server and Google's servers.
     */
    @Bean
    // public JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.client.provider.google.issuer-uri}") String issuerUri) {
    public JwtDecoder jwtDecoder(@Value("https://accounts.google.com") String issuerUri) {
       
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(issuerUri + "/.well-known/jwks.json").build();

        // Allow for a 5-minute clock skew
        OAuth2TokenValidator<Jwt> withClockSkew = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ofMinutes(5))
        );

        jwtDecoder.setJwtValidator(withClockSkew);

        return jwtDecoder;
    }

}
