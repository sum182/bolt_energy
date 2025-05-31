package com.boltenergy.service;

import com.boltenergy.config.WebClientConfig;
import com.boltenergy.exception.RalieDownloadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link AneelRalieService}.
 */
@ExtendWith(MockitoExtension.class)
class AneelRalieServiceTest {

    @Mock
    private WebClientConfig webClientConfig;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AneelRalieService aneelRalieService;

    @BeforeEach
    void setUp() {
        when(webClientConfig.createWebClient(anyString())).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        
        // Initialize the service manually to inject mocks
        aneelRalieService = new AneelRalieService(webClientConfig);
        aneelRalieService.init();
    }

    @Test
    void downloadRalieCsv_Success() throws IOException {
        // Arrange
        String csvContent = "id,nome,cnpj\n1,Usina Teste,12345678000199";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("dummy"));
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just(csvContent.getBytes()));
        
        // Act
        String result = aneelRalieService.downloadRalieCsv();
        
        // Assert
        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
        
        // Cleanup
        Files.deleteIfExists(Path.of(result));
    }

    @Test
    void downloadRalieCsv_ApiError_ThrowsException() {
        // Arrange
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new WebClientResponseException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        null,
                        null,
                        null
                )));
        
        // Act & Assert
        assertThrows(RalieDownloadException.class, aneelRalieService::downloadRalieCsv);
    }

    @Test
    void downloadRalieCsv_DownloadError_ThrowsException() {
        // Arrange
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("dummy"));
        when(responseSpec.bodyToMono(byte[].class))
                .thenReturn(Mono.error(new WebClientResponseException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Download Error",
                        null,
                        null,
                        null
                )));
        
        // Act & Assert
        assertThrows(RalieDownloadException.class, aneelRalieService::downloadRalieCsv);
    }
}
