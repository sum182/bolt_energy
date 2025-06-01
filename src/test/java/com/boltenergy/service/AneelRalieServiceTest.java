package com.boltenergy.service;

import com.boltenergy.config.WebClientConfig;
import com.boltenergy.exception.RalieDownloadException;
import com.boltenergy.model.RalieMetadata;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AneelRalieServiceTest {

    @Mock
    private WebClientConfig webClientConfig;

    @Mock
    private RalieMetadataService metadataService;
    
    @Mock
    private RalieUsinaCsvImportService csvImportService;
    
    @Mock
    private RalieUsinaEmpresaPotenciaGeradaService potenciaGeradaService;

    private WebClient webClient;
    private ExchangeFunction exchangeFunction;

    private AneelRalieService aneelRalieService;

    @BeforeEach
    void setUp() {
        exchangeFunction = mock(ExchangeFunction.class);
        webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();
        
        when(webClientConfig.createWebClient(anyString())).thenReturn(webClient);
        
        RalieMetadata metadata = new RalieMetadata();
        when(metadataService.loadMetadata()).thenReturn(metadata);
        
        aneelRalieService = new AneelRalieService(
            webClientConfig, 
            metadataService, 
            csvImportService, 
            potenciaGeradaService
        );
        
        try {
            Path tempDir = Files.createTempDirectory("test-downloads");
            doNothing().when(metadataService).init(any(Path.class));
            aneelRalieService.init();
        } catch (IOException e) {
            throw new RuntimeException("Falha ao configurar o ambiente de teste", e);
        }
    }

    private void mockHeadRequest(HttpStatus status, String etag, String lastModified) {
        when(exchangeFunction.exchange(any()))
            .thenAnswer(invocation -> {
                org.springframework.web.reactive.function.client.ClientRequest request = invocation.getArgument(0);
                if (request != null && request.method() == HttpMethod.HEAD) {
                    return Mono.just(ClientResponse.create(status)
                        .header(HttpHeaders.ETAG, etag)
                        .header(HttpHeaders.LAST_MODIFIED, lastModified)
                        .build());
                } else if (request != null && request.method() == HttpMethod.GET) {
                    DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                    DataBuffer buffer = dataBufferFactory.wrap("id,nome,cnpj\n1,Usina Teste,12345678000199".getBytes());
                    
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                        .body(Flux.just(buffer))
                        .build());
                }
                return Mono.error(new RuntimeException("Unexpected request: " + (request != null ? request.method() : "null")));
            });
    }
    
    private void mockGetRequest(byte[] responseBody) {
        when(exchangeFunction.exchange(any()))
            .thenAnswer(invocation -> {
                org.springframework.web.reactive.function.client.ClientRequest request = invocation.getArgument(0);
                if (request != null && request.method() == HttpMethod.GET) {
                    DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                    DataBuffer buffer = dataBufferFactory.wrap(responseBody);
                    
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                        .body(Flux.just(buffer))
                        .build());
                } else if (request != null && request.method() == HttpMethod.HEAD) {
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.ETAG, "\"test-etag\"")
                        .header(HttpHeaders.LAST_MODIFIED, "test-last-modified")
                        .build());
                }
                return Mono.error(new RuntimeException("Unexpected request: " + (request != null ? request.method() : "null")));
            });
    }
    
    @Test
    void downloadRalieCsv_Success() throws IOException {
        Path tempDir = Files.createTempDirectory("test-downloads");
        try {
            when(exchangeFunction.exchange(any()))
                .thenAnswer(invocation -> {
                    org.springframework.web.reactive.function.client.ClientRequest request = invocation.getArgument(0);
                    if (request != null && request.method() == HttpMethod.HEAD) {
                        return Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.ETAG, "\"test-etag\"")
                            .header(HttpHeaders.LAST_MODIFIED, "test-last-modified")
                            .build());
                    } else if (request != null && request.method() == HttpMethod.GET) {
                        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                        DataBuffer buffer = dataBufferFactory.wrap("id,nome,cnpj\n1,Usina Teste,12345678000199".getBytes());
                        
                        return Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                            .body(Flux.just(buffer))
                            .build());
                    }
                    return Mono.error(new RuntimeException("Unexpected request: " + (request != null ? request.method() : "null")));
                });
            
            RalieMetadata metadata = new RalieMetadata();
            when(metadataService.loadMetadata()).thenReturn(metadata);
            
            ReflectionTestUtils.setField(aneelRalieService, "downloadPath", tempDir);
            
            String result = aneelRalieService.downloadRalieCsv();
            
            assertNotNull(result, "O resultado não deve ser nulo");
            assertTrue(Files.exists(Paths.get(result)), "O arquivo deve ter sido baixado");
            
            verify(metadataService, atLeastOnce()).saveMetadata(any(RalieMetadata.class));
        } finally {
            Files.walk(tempDir)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(java.io.File::delete);
        }
    }

    @Test
    void downloadRalieCsv_ApiError_ThrowsException() {
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
        
        assertThrows(RalieDownloadException.class, aneelRalieService::downloadRalieCsv);
    }

    @Test
    void downloadRalieCsv_DownloadError_ThrowsException() {
        mockHeadRequest(HttpStatus.OK, "\"test-etag\"", "test-last-modified");
        
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
        
        assertThrows(RalieDownloadException.class, aneelRalieService::downloadRalieCsv);
    }
    
    @Test
    void downloadRalieCsv_FileNotModified_ReturnsExistingFile() throws IOException {
        Path tempDir = Files.createTempDirectory("test-downloads");
        try {
            Path tempFile = Files.createFile(tempDir.resolve("test-file.csv"));
            String existingFilePath = tempFile.toAbsolutePath().toString();
            
            RalieMetadata existingMetadata = new RalieMetadata();
            existingMetadata.setEtag("\"test-etag\"");
            existingMetadata.setLastModified("test-last-modified");
            existingMetadata.setLastDownloadedFile(existingFilePath);
            when(metadataService.loadMetadata()).thenReturn(existingMetadata);
            
            mockHeadRequest(HttpStatus.OK, "\"test-etag\"", "test-last-modified");
            
            String result = aneelRalieService.downloadRalieCsv();
            
            assertNotNull(result, "O resultado não deve ser nulo");
            assertTrue(Files.exists(Paths.get(result)), "O arquivo deve existir");
            verify(metadataService, never()).saveMetadata(any(RalieMetadata.class));
        } finally {
            Files.walk(tempDir)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(java.io.File::delete);
        }
    }
    
    @Test
    void downloadRalieCsv_FileModified_DownloadsNewFile() {
        String csvContent = "id,nome,cnpj\n1,Usina Teste,12345678000199";
        String existingFilePath = "/caminho/para/arquivo/antigo.csv";
        
        RalieMetadata existingMetadata = new RalieMetadata();
        existingMetadata.setEtag("\"old-etag\"");
        existingMetadata.setLastModified("old-last-modified");
        existingMetadata.setLastDownloadedFile(existingFilePath);
        when(metadataService.loadMetadata()).thenReturn(existingMetadata);
        
        mockHeadRequest(HttpStatus.OK, "\"new-etag\"", "new-last-modified");
        mockGetRequest(csvContent.getBytes());
        
        String result = aneelRalieService.downloadRalieCsv();
        
        assertNotNull(result, "O resultado não deve ser nulo");
        assertNotEquals(existingFilePath, result, "Deveria ser um novo arquivo");
        verify(metadataService, atLeastOnce()).saveMetadata(any(RalieMetadata.class));
    }
}
