package com.example.gatewayservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfiguration corsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedHeader("DNT");
        config.addAllowedHeader("User-Agent");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("If-Modified-Since");
        config.addAllowedHeader("Cache-Control");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Range");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.addExposedHeader("Content-Length");
        config.addExposedHeader("Content-Range");
        config.setMaxAge(18000L);

        return config;
    }

    @Bean
    public CorsWebFilter corsWebFilter(CorsConfiguration corsConfiguration) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/intelligentSelector/**", corsConfiguration);
        return new CorsWebFilter(source);
    }
}
