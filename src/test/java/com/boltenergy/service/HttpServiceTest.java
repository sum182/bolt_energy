package com.boltenergy.service;

import com.boltenergy.config.WebClientConfig;
import com.boltenergy.config.WebClientProperties;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import io.netty.channel.ChannelOption;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.netty.http.client.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

class HttpServiceTest {

    private MockWebServer mockWebServer;
    private HttpService httpService;
    
    private WebClientConfig webClientConfig;
    
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        // Configura as propriedades do WebClient
        WebClientProperties properties = new WebClientProperties();
        properties.setConnectTimeout(Duration.ofSeconds(30));
        properties.setReadTimeout(Duration.ofSeconds(30));
        properties.setResponseTimeout(Duration.ofSeconds(30));
        properties.setMaxInMemorySize(DataSize.ofMegabytes(50));
        
        // Cria uma instância de WebClientConfig com as propriedades
        webClientConfig = new WebClientConfig(properties);
        
        // Configura o serviço
        httpService = new HttpService(webClientConfig);
        
        // Configura o WebClient com o MockWebServer
        WebClient webClient = webClientConfig.createWebClient(mockWebServer.url("/").toString());
        ReflectionTestUtils.setField(httpService, "webClient", webClient);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    
    @Test
    void get_WhenRequestIsSuccessful_ShouldReturnResponseBody() {
        // Arrange
        String expectedResponse = "{\"status\":\"success\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader("Content-Type", "application/json")
                .setBody(expectedResponse));
        
        String url = mockWebServer.url("/test").toString();
        
        // Act
        String response = httpService.get(url);
        
        // Assert
        assertEquals(expectedResponse, response);
    }
    
    @Test
    void get_WhenServerReturnsError_ShouldThrowException() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setBody("Internal Server Error"));
        
        String url = mockWebServer.url("/error").toString();
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            httpService.get(url);
        });
        
        assertTrue(exception.getMessage().contains("Request failed with status: 500"));
    }
    

}
