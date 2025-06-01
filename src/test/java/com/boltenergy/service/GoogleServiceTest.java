package com.boltenergy.service;

import com.boltenergy.config.WebClientProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleServiceTest {

    @Mock
    private WebClientProperties webClientProperties;
    
    @Mock
    private ExchangeFunction exchangeFunction;
    
    @InjectMocks
    private GoogleService googleService;
    
    private WebClient webClient;
    
    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .baseUrl("http://test.com")
                .exchangeFunction(exchangeFunction)
                .build();
        
        when(webClientProperties.getResponseTimeout())
                .thenReturn(Duration.ofSeconds(30));
                
        ReflectionTestUtils.setField(googleService, "webClient", webClient);
    }
    
    @Test
    void fetchGoogleHomepage_shouldReturnContent_whenRequestIsSuccessful() {
        String expectedContent = "<html>Test Content</html>";
        
        mockWebClientResponse(HttpStatus.OK, expectedContent);
        
        String result = googleService.fetchGoogleHomepage();
        
        assertNotNull(result);
        assertEquals(expectedContent, result);
    }
    
    @Test
    void fetchGoogleHomepage_shouldThrowException_whenRequestFails() {
        mockWebClientErrorResponse(new RuntimeException("Connection error"));
        
        assertThrows(RuntimeException.class, () -> googleService.fetchGoogleHomepage());
    }
    
    @Test
    void fetchGoogleHomepageAsync_shouldReturnMonoWithContent_whenRequestIsSuccessful() {
        String expectedContent = "<html>Async Test</html>";
        
        mockWebClientResponse(HttpStatus.OK, expectedContent);
        
        Mono<String> result = googleService.fetchGoogleHomepageAsync();
        
        assertNotNull(result);
        assertEquals(expectedContent, result.block());
    }
    
    @Test
    void fetchGoogleHomepageAsync_shouldReturnErrorMono_whenRequestFails() {
        String errorMessage = "Connection failed";
        mockWebClientErrorResponse(new RuntimeException(errorMessage));
        
        Mono<String> result = googleService.fetchGoogleHomepageAsync();
        
        assertNotNull(result);
        assertThrows(RuntimeException.class, result::block);
    }
    
    private void mockWebClientResponse(HttpStatus status, String body) {
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.statusCode()).thenReturn(status);
        when(clientResponse.bodyToMono(String.class))
                .thenReturn(Mono.just(body));
                
        when(exchangeFunction.exchange(any()))
                .thenReturn(Mono.just(clientResponse));
    }
    
    private void mockWebClientErrorResponse(Throwable error) {
        when(exchangeFunction.exchange(any()))
                .thenReturn(Mono.error(error));
    }
}
