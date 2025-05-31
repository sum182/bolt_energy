package com.boltenergy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service for making HTTP requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HttpService {

    private final WebClient webClient;

    /**
     * Makes a GET request to the specified URL and returns the response as a String.
     *
     * @param url The URL to make the request to
     * @return The response body as a String
     */
    public String get(String url) {
        log.info("Making GET request to: {}", url);
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(
                        status -> status != HttpStatus.OK,
                        response -> Mono.error(new RuntimeException("Request failed with status: " + response.statusCode()))
                )
                .bodyToMono(String.class)
                .block();
    }
}
