package com.boltenergy.controller;

import com.boltenergy.service.GoogleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TestControllerTest {

    private static final String MOCK_HTML = "<html><body>Google Test Page</body></html>";
    private static final String ERROR_MESSAGE = "Falha ao acessar o Google";
    private static final String WELCOME_MESSAGE = "Bem-vindo ao endpoint de teste da Bolt Energy!";

    @Mock
    private GoogleService googleService;

    @InjectMocks
    private TestController testController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenHelloEndpointCalled_thenReturnWelcomeMessage() {
        ResponseEntity<String> response = testController.hello();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(WELCOME_MESSAGE, response.getBody());
    }

    @Test
    void givenSuccessfulResponse_whenGetGoogleHomepage_thenReturnHtml() {
        when(googleService.fetchGoogleHomepage()).thenReturn(MOCK_HTML);

        ResponseEntity<String> response = testController.getGoogleHomepage();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().includes(MediaType.TEXT_HTML));
        assertEquals(MOCK_HTML, response.getBody());
    }

    @Test
    void givenServiceError_whenGetGoogleHomepage_thenReturnErrorResponse() {
        when(googleService.fetchGoogleHomepage()).thenThrow(new RuntimeException(ERROR_MESSAGE));

        ResponseEntity<String> response = testController.getGoogleHomepage();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
        assertTrue(response.getBody().contains(ERROR_MESSAGE));
    }

    @Test
    void givenSuccessfulResponse_whenGetGoogleHomepageAsync_thenReturnHtml() {
        when(googleService.fetchGoogleHomepageAsync()).thenReturn(Mono.just(MOCK_HTML));

        Mono<ResponseEntity<String>> result = testController.getGoogleHomepageAsync();

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertTrue(response.getHeaders().getContentType().includes(MediaType.TEXT_HTML));
                assertEquals(MOCK_HTML, response.getBody());
            })
            .verifyComplete();
    }

    @Test
    void givenServiceError_whenGetGoogleHomepageAsync_thenReturnErrorResponse() {
        when(googleService.fetchGoogleHomepageAsync())
            .thenReturn(Mono.error(new RuntimeException(ERROR_MESSAGE)));

        Mono<ResponseEntity<String>> result = testController.getGoogleHomepageAsync();

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                assertTrue(response.getHeaders().getContentType().includes(MediaType.TEXT_PLAIN));
                assertTrue(response.getBody().contains(ERROR_MESSAGE));
            })
            .verifyComplete();
    }
}
