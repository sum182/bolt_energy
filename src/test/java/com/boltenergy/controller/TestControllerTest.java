package com.boltenergy.controller;

import com.boltenergy.service.GoogleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para o TestController.
 */
class TestControllerTest {

    @Mock
    private GoogleService googleService;

    @InjectMocks
    private TestController testController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Testa o método hello() para garantir que retorne a mensagem de boas-vindas correta.
     */
    @Test
    void hello_ShouldReturnWelcomeMessage() {
        // Act
        ResponseEntity<String> response = testController.hello();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Bem-vindo ao endpoint de teste da Bolt Energy!", response.getBody());
    }

    /**
     * Testa o método getGoogleHomepage() para garantir que retorne o HTML da página do Google.
     */
    @Test
    void getGoogleHomepage_ShouldReturnGoogleHomepage() {
        // Arrange
        String mockHtml = "<html><body>Google Test Page</body></html>";
        when(googleService.fetchGoogleHomepage())
                .thenReturn(Mono.just(mockHtml));

        // Act
        Mono<ResponseEntity<String>> result = testController.getGoogleHomepage();

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getHeaders().getContentType().includes(org.springframework.http.MediaType.TEXT_HTML));
                    assertEquals(mockHtml, response.getBody());
                })
                .verifyComplete();
    }

    /**
     * Testa o método getGoogleHomepage() para garantir que retorne um erro quando o serviço do Google falhar.
     */
    @Test
    void getGoogleHomepage_ShouldReturnErrorWhenServiceFails() {
        // Arrange
        String errorMessage = "Falha ao acessar o Google";
        when(googleService.fetchGoogleHomepage())
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        // Act
        Mono<ResponseEntity<String>> result = testController.getGoogleHomepage();

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertTrue(response.getHeaders().getContentType().includes(org.springframework.http.MediaType.TEXT_PLAIN));
                    assertTrue(response.getBody().contains("[TEST] Erro ao acessar o Google"));
                })
                .verifyComplete();
    }
}
