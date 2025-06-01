package com.boltenergy.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for WebClient.
 * Provides a generic WebClient builder with common configurations.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final WebClientProperties properties;



    /**
     * Creates a generic WebClient builder with default configurations.
     * The base URL should be set when using the builder.
     *
     * @return Configured WebClient.Builder
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = createHttpClient(properties.getConnectTimeout().toSeconds());
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.USER_AGENT, "BoltEnergyApp/1.0")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize((int) properties.getMaxInMemorySize().toBytes()));
    }

    /**
     * Creates a WebClient with a specific base URL.
     *
     * @param baseUrl the base URL for the WebClient
     * @return Configured WebClient
     */
    public WebClient createWebClient(String baseUrl) {
        return createWebClient(baseUrl, properties.getConnectTimeout().toSeconds());
    }

    /**
     * Creates a WebClient with a specific base URL and timeout.
     *
     * @param baseUrl the base URL for the WebClient
     * @param timeoutInSeconds timeout in seconds
     * @return Configured WebClient
     */
    public WebClient createWebClient(String baseUrl, long timeoutInSeconds) {
        HttpClient httpClient = createHttpClient(timeoutInSeconds);
        
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.USER_AGENT, "BoltEnergyApp/1.0")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize((int) properties.getMaxInMemorySize().toBytes()))
                .build();
    }

    private HttpClient createHttpClient(long timeoutInSeconds) {
        return HttpClient.create()
                // Configurações de buffer
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) Duration.ofSeconds(timeoutInSeconds).toMillis())
                .responseTimeout(Duration.ofSeconds(timeoutInSeconds))
                // Aumenta o tamanho dos buffers
                .option(ChannelOption.SO_RCVBUF, (int) properties.getBufferSize().toBytes())
                .option(ChannelOption.SO_SNDBUF, (int) properties.getBufferSize().toBytes())
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeout().toSeconds(), TimeUnit.SECONDS))
                           .addHandlerLast(new WriteTimeoutHandler(timeoutInSeconds, TimeUnit.SECONDS)))
                .compress(true);
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            if (log.isDebugEnabled()) {
                clientRequest.headers().forEach((name, values) -> 
                    values.forEach(value -> log.debug("{}: {}", name, value))
                );
            }
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("Response status: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
