//package com.auth.service.config;
//
//import org.keycloak.admin.client.Keycloak;
//import org.keycloak.admin.client.KeycloakBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class KeycloakConfig {
//
//    private final KeycloakProperties properties;
//
//    public KeycloakConfig(KeycloakProperties properties) {
//        this.properties = properties;
//    }
//
//    @Bean
//    public Keycloak keycloak() {
//        return KeycloakBuilder.builder()
//                .serverUrl(properties.getServerUrl())
//                .realm(properties.getRealm())
//                .clientId(properties.getClientId())
//                .clientSecret(properties.getClientSecret())
//                .grantType("client_credentials")
//                .build();
//    }
//}
