package com.secureauth.config;

import com.secureauth.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that exposes the existing {@link AuthService}
 * as a bean without modifying the service class itself.
 */
@Configuration
public class ServiceConfig {

    @Bean
    public AuthService authService() {
        return new AuthService();
    }
}
