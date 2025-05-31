package com.boltenergy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Service for interacting with Google services.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleService {

    private final WebClient webClient;

    /**
     * Fetches the Google homepage.
     *
     * @return A Mono containing the HTML content of the Google homepage
     */
    public Mono<String> fetchGoogleHomepage() {
        log.info("Fetching Google homepage");
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/").build())
                .retrieve()
                .onStatus(
                    status -> status.isError(),
                    response -> {
                        String errorMessage = String.format(
                            "Failed to fetch Google homepage. Status: %s",
                            response.statusCode()
                        );
                        log.error(errorMessage);
                        return Mono.error(new RuntimeException(errorMessage));
                    }
                )
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(response -> log.info("Successfully fetched Google homepage"))
                .doOnError(error -> log.error("Error fetching Google homepage: {}", error.getMessage()));
    }
}
