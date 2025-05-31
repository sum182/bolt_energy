package com.boltenergy.service;

import com.boltenergy.config.WebClientConfig;
import com.boltenergy.exception.RalieDownloadException;
import com.boltenergy.model.RalieMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
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
    private RalieMetadataService metadataService;
    
    @Mock
    private ObjectMapper objectMapper;

    private WebClient webClient;
    private ExchangeFunction exchangeFunction;

    @InjectMocks
    private AneelRalieService aneelRalieService;

    @BeforeEach
    void setUp() {
        // Configuração do WebClient para testes
        exchangeFunction = mock(ExchangeFunction.class);
        webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();
        
        when(webClientConfig.createWebClient(anyString())).thenReturn(webClient);
        
        // Mock do RalieMetadata
        RalieMetadata metadata = new RalieMetadata();
        when(metadataService.loadMetadata()).thenReturn(metadata);
        
        // Initialize the service manually to inject mocks
        aneelRalieService = new AneelRalieService(webClientConfig, metadataService, objectMapper);
        
        // Mock do init para não tentar criar diretórios reais
        try {
            // Cria um diretório temporário para os testes
            Path tempDir = Files.createTempDirectory("test-downloads");
            doNothing().when(metadataService).init(any(Path.class));
            aneelRalieService.init();
        } catch (IOException e) {
            throw new RuntimeException("Falha ao configurar o ambiente de teste", e);
        }
    }

    /*private void mockHeadRequest(HttpStatus status, String etag, String lastModified) {
        when(exchangeFunction.exchange(any()))
            .thenAnswer(invocation -> {
                org.springframework.web.reactive.function.client.ClientRequest request = invocation.getArgument(0);
                if (request.method() == HttpMethod.HEAD) {
                    return Mono.just(ClientResponse.create(status)
                        .header(HttpHeaders.ETAG, etag)
                        .header(HttpHeaders.LAST_MODIFIED, lastModified)
                        .build());
                }
                return Mono.error(new RuntimeException("Unexpected request"));
            });
    }
    
    private void mockGetRequest(byte[] responseBody) {
        when(exchangeFunction.exchange(any()))
            .thenAnswer(invocation -> {
                org.springframework.web.reactive.function.client.ClientRequest request = invocation.getArgument(0);
                if (request.method() == HttpMethod.GET) {
                    DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                    DataBuffer buffer = dataBufferFactory.wrap(responseBody);
                    
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                        .body(Flux.just(buffer))
                        .build());
                }
                return Mono.error(new RuntimeException("Unexpected request"));
            });
    }
    
    @Test
    void downloadRalieCsv_Success() {
        // Arrange
        String csvContent = "id,nome,cnpj\n1,Usina Teste,12345678000199";
        
        // Configura os mocks para as requisições
        mockHeadRequest(HttpStatus.OK, "\"test-etag\"", "test-last-modified");
        mockGetRequest(csvContent.getBytes());
        
        // Mock do serviço de metadados
        RalieMetadata metadata = new RalieMetadata();
        when(metadataService.loadMetadata()).thenReturn(metadata);
        
        // Act
        String result = aneelRalieService.downloadRalieCsv();
        
        // Assert
        assertNotNull(result, "O resultado não deve ser nulo");
        
        // Verifica se o método saveMetadata foi chamado
        verify(metadataService, atLeastOnce()).saveMetadata(any(RalieMetadata.class));
    }

    @Test
    void downloadRalieCsv_ApiError_ThrowsException() {
        // Arrange
        // Configura o mock para retornar erro na requisição HEAD
        when(exchangeFunction.exchange(any()))
            .thenAnswer(invocation -> {
                org.springframework.web.reactive.function.client.ClientRequest request = invocation.getArgument(0);
                if (request.method() == HttpMethod.HEAD) {
                    return Mono.error(new WebClientResponseException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        null,
                        null,
                        null
                    ));
                }
                return Mono.error(new RuntimeException("Unexpected request"));
            });
        
        // Act & Assert
        assertThrows(RalieDownloadException.class, aneelRalieService::downloadRalieCsv);
    }

    @Test
    void downloadRalieCsv_DownloadError_ThrowsException() {
        // Arrange
        // Configura o mock para a requisição HEAD
        mockHeadRequest(HttpStatus.OK, "\"test-etag\"", "test-last-modified");
        
        // Configura o erro na requisição GET
        when(exchangeFunction.exchange(any()))
            .thenAnswer(invocation -> {
                org.springframework.web.reactive.function.client.ClientRequest request = invocation.getArgument(0);
                if (request.method() == HttpMethod.HEAD) {
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.ETAG, "\"test-etag\"")
                        .header(HttpHeaders.LAST_MODIFIED, "test-last-modified")
                        .build());
                } else if (request.method() == HttpMethod.GET) {
                    return Mono.error(new WebClientResponseException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Download Error",
                        null,
                        null,
                        null
                    ));
                }
                return Mono.error(new RuntimeException("Unexpected request"));
            });
        
        // Act & Assert
        assertThrows(RalieDownloadException.class, aneelRalieService::downloadRalieCsv);
    }
    
    @Test
    void downloadRalieCsv_FileNotModified_ReturnsExistingFile() {
        // Arrange
        String existingFilePath = "/caminho/para/arquivo/existente.csv";
        
        // Configura os metadados existentes
        RalieMetadata existingMetadata = new RalieMetadata();
        existingMetadata.setEtag("\"test-etag\"");
        existingMetadata.setLastModified("test-last-modified");
        existingMetadata.setLastDownloadedFile(existingFilePath);
        when(metadataService.loadMetadata()).thenReturn(existingMetadata);
        
        // Configura o mock para a requisição HEAD com os mesmos cabeçalhos
        mockHeadRequest(HttpStatus.OK, "\"test-etag\"", "test-last-modified");
        
        // Act
        String result = aneelRalieService.downloadRalieCsv();
        
        // Assert
        assertEquals(existingFilePath, result, "Deveria retornar o caminho do arquivo existente");
        verify(metadataService, never()).saveMetadata(any(RalieMetadata.class));
    }
    
    @Test
    void downloadRalieCsv_FileModified_DownloadsNewFile() {
        // Arrange
        String csvContent = "id,nome,cnpj\n1,Usina Teste,12345678000199";
        String existingFilePath = "/caminho/para/arquivo/antigo.csv";
        
        // Configura os metadados existentes
        RalieMetadata existingMetadata = new RalieMetadata();
        existingMetadata.setEtag("\"old-etag\"");
        existingMetadata.setLastModified("old-last-modified");
        existingMetadata.setLastDownloadedFile(existingFilePath);
        when(metadataService.loadMetadata()).thenReturn(existingMetadata);
        
        // Configura o mock para a requisição HEAD com novos cabeçalhos
        mockHeadRequest(HttpStatus.OK, "\"new-etag\"", "new-last-modified");
        
        // Configura o mock para a requisição GET
        mockGetRequest(csvContent.getBytes());
        
        // Act
        String result = aneelRalieService.downloadRalieCsv();
        
        // Assert
        assertNotNull(result, "O resultado não deve ser nulo");
        assertNotEquals(existingFilePath, result, "Deveria ser um novo arquivo");
        verify(metadataService, atLeastOnce()).saveMetadata(any(RalieMetadata.class));
    }*/
}
