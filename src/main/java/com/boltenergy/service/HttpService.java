package com.boltenergy.service;

import com.boltenergy.config.WebClientConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service for making HTTP requests.
 * Uses a generically configured WebClient.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HttpService {

    private final WebClientConfig webClientConfig;
    private WebClient webClient;
    
    @PostConstruct
    public void init() {
        // Create a generic WebClient without a base URL since this service can make requests to any URL
        this.webClient = webClientConfig.webClientBuilder().build();
    }

    /**
     * Makes a GET request to the specified URL and returns the response as a String.
     *
     * @param url The full URL to make the request to
     * @return The response body as a String
     * @throws RuntimeException if the request fails or returns a non-200 status code
     */
    public String get(String url) {
        log.info("Making GET request to: {}", url);
        
        try {
            return webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(
                            status -> status != HttpStatus.OK,
                            response -> Mono.error(new RuntimeException("Request failed with status: " + response.statusCode()))
                    )
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Error making GET request to {}: {}", url, e.getMessage());
            throw new RuntimeException("Failed to make GET request: " + e.getMessage(), e);
        }
    }
}
