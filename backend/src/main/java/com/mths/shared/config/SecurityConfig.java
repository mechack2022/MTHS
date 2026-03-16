package com.mths.shared.config;

import com.mths.shared.jwt.JwtAuthenticationEntryPoint;
import com.mths.shared.jwt.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableAspectJAutoProxy
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Explicitly permit auth endpoints with correct HTTP methods
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/verify-email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/resend-verification-code").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/initiate-password-reset").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/resend-password-reset-code").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/verify-reset-password-code").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        
                        // Profile creation token endpoints (no authentication required)
                        .requestMatchers(HttpMethod.POST, "/api/auth/generate-profile-token").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/profile-creation-status/*").permitAll()

                        
                        // Public profile creation endpoints (no authentication required)
                        .requestMatchers(HttpMethod.POST, "/api/v1/profile/public/patient/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/profile/public/doctor/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/profile/public/lab-technician/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/profile/public/pharmacist/*").permitAll()

//                        .requestMatchers(HttpMethod.POST, "/api/v1/fileUpload/profile-image").permitAll()

                        // Webhook endpoints (no authentication required - verified via signature)
                        .requestMatchers(HttpMethod.POST, "/api/webhooks/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/webhooks/**").permitAll()

                        // WebSocket endpoints (no authentication required for connection)
                        .requestMatchers("/ws/**").permitAll()

                        // Permit other existing endpoints
                        .requestMatchers("/users/verify").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        // Swagger/OpenAPI endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Frontend origins - Update these based on your actual frontend URLs
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",      // React development server
            "http://localhost:5173",      // Vite development server
            "http://localhost:8080",      // Vue.js development server
            "http://localhost:4200",      // Angular development server
            "http://localhost:8081",      // Backend server (for testing)
            "null"                         // For local file:// access during testing
        ));
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "https://*.netlify.app",      // Netlify deployments
            "https://*.vercel.app",       // Vercel deployments
            "https://*.herokuapp.com"     // Heroku deployments
        ));
        // HTTP methods that frontend can use
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Profile-Creation-Token",
            "X-API-Key",
            "Cache-Control"
        ));

        // Headers that frontend can read from response
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Disposition",
            "X-Total-Count",
            "X-Page-Number",
            "X-Page-Size",
            "X-Total-Pages"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight response for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            web.ignoring().requestMatchers(
                    HttpMethod.OPTIONS, "/**"
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}