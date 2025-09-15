package com.example.momskitchen.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@ConfigurationProperties(prefix = "cors")
public class CorsConfig {

    private String allowedOrigins;
    private String allowedMethods;
    private String allowedHeaders;
    private boolean allowCredentials;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(allowCredentials);
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

    // Getters and setters
    public String getAllowedOrigins() { return allowedOrigins; }
    public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }

    public String getAllowedMethods() { return allowedMethods; }
    public void setAllowedMethods(String allowedMethods) { this.allowedMethods = allowedMethods; }

    public String getAllowedHeaders() { return allowedHeaders; }
    public void setAllowedHeaders(String allowedHeaders) { this.allowedHeaders = allowedHeaders; }

    public boolean isAllowCredentials() { return allowCredentials; }
    public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }
}
