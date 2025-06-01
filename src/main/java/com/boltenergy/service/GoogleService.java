package com.boltenergy.service;

import com.boltenergy.config.WebClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleService {

    private static final String GOOGLE_BASE_URL = "https://www.google.com";
    
    private final WebClientProperties webClientProperties;
    private WebClient webClient;
    
    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(GOOGLE_BASE_URL)
                .defaultHeader("Accept", MediaType.TEXT_HTML_VALUE)
                .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                        .responseTimeout(webClientProperties.getResponseTimeout())
                ))
                .build();
    }

    public String fetchGoogleHomepage() {
        try {
            log.info("Buscando página inicial do Google...");
            
            return webClient.get()
                    .uri("/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(webClientProperties.getResponseTimeout())
                    .block();
                    
        } catch (Exception e) {
            log.error("Erro ao buscar página do Google: {}", e.getMessage());
            throw new RuntimeException("Falha ao acessar o Google: " + e.getMessage(), e);
        }
    }

    public Mono<String> fetchGoogleHomepageAsync() {
        log.info("Starting async fetch of Google homepage");
        
        return webClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(webClientProperties.getResponseTimeout())
                .doOnSuccess(html -> log.info("Successfully fetched Google homepage asynchronously"))
                .doOnError(error -> log.error("Error fetching Google homepage asynchronously: {}", error.getMessage()));
    }
}
