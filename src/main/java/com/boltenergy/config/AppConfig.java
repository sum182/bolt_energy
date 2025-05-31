package com.boltenergy.config;

import com.boltenergy.service.RalieMetadataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration class.
 * Configures and provides beans for the application.
 */
@Configuration
public class AppConfig {
    
    /**
     * Creates and configures the RalieMetadataService bean.
     * 
     * @return A new instance of RalieMetadataService
     */
    @Bean
    public RalieMetadataService ralieMetadataService() {
        return new RalieMetadataService(new ObjectMapper());
    }
    
    /**
     * Provides the ObjectMapper bean for JSON processing.
     * 
     * @return A new instance of ObjectMapper with default configuration
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
