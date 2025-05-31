package com.boltenergy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.boltenergy.config.WebClientProperties;

/**
 * Classe principal da aplicação Bolt Energy
 */
@SpringBootApplication
@EnableConfigurationProperties(WebClientProperties.class)
public class App {
    
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
        logger.info("Aplicação Bolt Energy iniciada com sucesso!");
        logger.info("Java version: {}", System.getProperty("java.version"));
    }
}
