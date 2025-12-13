package com.mths.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * Configures API documentation for the MTHS application
 *
 * Access Swagger UI at: http://localhost:8081/swagger-ui/index.html
 * Access OpenAPI JSON at: http://localhost:8081/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }

    /**
     * API Information
     */
    private Info apiInfo() {
        return new Info()
                .title("MTHS - My Team Health Service API")
                .version("1.0.0")
                .description("""
                        ### Medical & Telemedicine Healthcare System

                        A comprehensive healthcare platform providing:
                        - 🏥 Patient appointment booking with payment integration
                        - 👨‍⚕️ Doctor consultation management
                        - 💳 Paystack payment processing
                        - 🔐 JWT-based authentication
                        - 📹 Video consultation support
                        - 📊 Patient vital signs tracking
                        - 🧪 Lab technician integration

                        **Authentication**: All endpoints (except public ones) require JWT Bearer token.

                        **Payment Flow**:
                        1. Patient initiates booking → Payment initialization
                        2. Patient completes payment via Paystack
                        3. Webhook creates appointment
                        4. Doctor confirms appointment
                        5. Consultation takes place

                        **Base URL**: http://localhost:8081
                        """)
                .contact(apiContact())
                .license(apiLicense());
    }

    /**
     * Contact Information
     */
    private Contact apiContact() {
        return new Contact()
                .name("MTHS Development Team")
                .email("support@mths.com")
                .url("https://github.com/your-org/mths");
    }

    /**
     * License Information
     */
    private License apiLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * API Servers
     */
    private List<Server> apiServers() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Local Development Server");

        Server productionServer = new Server()
                .url("https://api.mths.com")
                .description("Production Server");

        return List.of(localServer, productionServer);
    }

    /**
     * Security Configuration for JWT Authentication
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("bearer-jwt", jwtSecurityScheme());
    }

    /**
     * JWT Security Scheme
     */
    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .name("bearer-jwt")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("""
                        ### JWT Authentication

                        Enter your JWT token in the format: `your-jwt-token`

                        **How to get a token:**
                        1. Register via `/api/auth/register`
                        2. Verify email via `/api/auth/verify-email`
                        3. Login via `/api/auth/login`
                        4. Copy the `accessToken` from response
                        5. Click 'Authorize' button above and paste the token

                        **Token format**: Do NOT include "Bearer" prefix, just paste the token
                        """);
    }

    /**
     * Security Requirement (applies JWT to all endpoints by default)
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("bearer-jwt");
    }
}
