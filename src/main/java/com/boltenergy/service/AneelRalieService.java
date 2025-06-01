package com.boltenergy.service;

import com.boltenergy.config.WebClientConfig;
import com.boltenergy.exception.RalieDownloadException;
import com.boltenergy.model.RalieMetadata;
import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Service for downloading and processing RALIE data from ANEEL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AneelRalieService {


//    private static final String ANEEL_BASE_URL = "https://dadosabertos.aneel.gov.br";
    // URL direta para o arquivo CSV do RALIE (atualizada para o recurso correto)
//    private static final String RALIE_CSV_URL = "https://dadosabertos.aneel.gov.br/dataset/57e4b8b5-a5db-40e6-9901-27ca629d0477/resource/4a615df8-4c25-48fa-bbea-873a36a79518/download/ralie-usina.csv";


    //urls de teste
    private static final String ANEEL_BASE_URL = "https://github.com";
    private static final String RALIE_CSV_URL = "https://raw.githubusercontent.com/sum182/bolt_energy/refs/heads/main/src/main/resources/examples/ralie-usina-example-simple.csv";
    private static final String DOWNLOAD_DIR = "downloads";
    
    private final WebClientConfig webClientConfig;
    private final RalieMetadataService metadataService;
    private WebClient webClient;
    private Path appBasePath;
    private Path downloadPath;
    private RalieMetadata metadata;
    
    @PostConstruct
    public void init() {
        this.webClient = webClientConfig.createWebClient(ANEEL_BASE_URL);
        this.appBasePath = Paths.get("").toAbsolutePath();
        this.downloadPath = appBasePath.resolve(DOWNLOAD_DIR);
        
        try {
            log.info("Inicializando diretório de downloads em: {}", this.downloadPath);
            
            if (!Files.exists(this.downloadPath)) {
                Files.createDirectories(this.downloadPath);
                log.info("Diretório de downloads criado com sucesso");
            }
            
            if (!Files.isWritable(this.downloadPath)) {
                throw new IOException("Sem permissão de escrita no diretório: " + this.downloadPath);
            }
            
            metadataService.init(this.downloadPath);
            this.metadata = metadataService.loadMetadata();
            
            log.info("Metadados carregados. Último download em: {}", 
                    metadata.getFormattedLastDownloadTime());
                    
        } catch (IOException e) {
            log.error("Erro ao configurar o diretório de downloads: {}", e.getMessage(), e);
            throw new RalieDownloadException("Falha ao configurar o diretório de downloads: " + e.getMessage(), e);
        }
    }
    
    /**
     * Downloads the RALIE CSV file only if it has changed since the last download.
     * 
     * @return Path to the downloaded file or the existing file if no changes
     */
    public String downloadRalieCsv() {
        log.info("Iniciando verificação de atualizações do arquivo RALIE da ANEEL");
        
        try {
            String csvUrl = getCsvFileUrl();
            log.info("URL do arquivo CSV: {}", csvUrl);
            
            this.metadata = metadataService.loadMetadata();
            
            if (!hasRemoteFileChanged(csvUrl)) {
                log.info("O arquivo remoto não foi modificado desde o último download");
                return findLatestRalieFile()
                    .map(Path::toString)
                    .orElseGet(() -> downloadNewFile(csvUrl));
            }
            
            log.info("Arquivo remoto foi modificado ou é o primeiro download");
            return downloadNewFile(csvUrl);
            
        } catch (RalieDownloadException e) {
            log.error("Erro ao baixar o arquivo RALIE: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao baixar o arquivo RALIE: {}", e.getMessage(), e);
            throw new RalieDownloadException("Erro ao baixar o arquivo RALIE: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica se o arquivo remoto foi modificado desde o último download
     */
    private boolean hasRemoteFileChanged(String fileUrl) {
        try {
            if (metadata.getEtag() == null && metadata.getLastModified() == null) {
                log.info("Nenhum ETag ou Last-Modified anterior encontrado, considerando como modificado");
                return true;
            }
            
            HttpHeaders headers = webClient.head()
                .uri(fileUrl)
                .retrieve()
                .toBodilessEntity()
                .block()
                .getHeaders();
            
            String currentEtag = headers.getETag();
            String currentLastModified = headers.getFirst("Last-Modified");
            
            log.debug("Cabeçalhos recebidos - ETag: {}, Last-Modified: {}", currentEtag, currentLastModified);
            
            if (currentEtag == null && currentLastModified == null) {
                log.info("Servidor não retornou ETag nem Last-Modified, considerando como modificado");
                return true;
            }
            
            if (metadata.getEtag() != null && currentEtag != null) {
                boolean etagChanged = !currentEtag.equals(metadata.getEtag());
                log.info("Comparação de ETag - Antigo: {}, Novo: {}, Modificado: {}", 
                        metadata.getEtag(), currentEtag, etagChanged);
                return etagChanged;
            }
            
            if (metadata.getLastModified() != null && currentLastModified != null) {
                boolean lastModifiedChanged = !currentLastModified.equals(metadata.getLastModified());
                log.info("Comparação de Last-Modified - Antigo: {}, Novo: {}, Modificado: {}", 
                        metadata.getLastModified(), currentLastModified, lastModifiedChanged);
                return lastModifiedChanged;
            }
            
            log.info("Não foi possível determinar se o arquivo foi modificado. Considerando como modificado.");
            return true;
            
        } catch (Exception e) {
            log.warn("Erro ao verificar alterações no arquivo remoto: {}. Considerando como modificado.", e.getMessage());
            return true;
        }
    }
    
    /**
     * Baixa um novo arquivo e atualiza os metadados
     */
    @Transactional(rollbackFor = Exception.class)
    private String downloadNewFile(String fileUrl) {
        try {
            log.info("Iniciando download de novo arquivo: {}", fileUrl);
            
            HttpHeaders headers = webClient.head()
                .uri(fileUrl)
                .retrieve()
                .toBodilessEntity()
                .block()
                .getHeaders();
                
            String etag = headers.getETag();
            String lastModified = headers.getFirst("Last-Modified");
            log.info("Novos cabeçalhos recebidos - ETag: {}, Last-Modified: {}", etag, lastModified);
                
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("ralie_%s.csv", timestamp);
            Path filePath = downloadPath.resolve(fileName);
            
            log.info("Iniciando download para: {}", filePath);
            long startTime = System.currentTimeMillis();
            
            downloadFile(fileUrl, filePath);
            
            long fileSize = Files.size(filePath);
            double duration = (System.currentTimeMillis() - startTime) / 1000.0;
            
            log.info("Download concluído em {}s - Tamanho: {}MB", 
                    String.format("%.2f", duration), 
                    String.format("%.2f", fileSize / (1024.0 * 1024.0)));
            
            metadata.update(etag, lastModified, filePath.toString(), fileSize);
            metadataService.saveMetadata(metadata);
            this.metadata = metadataService.loadMetadata();
            
            log.info("Metadados atualizados com sucesso");
            
            return filePath.toString();
            
        } catch (Exception e) {
            log.error("Erro ao baixar novo arquivo: {}", e.getMessage(), e);
            throw new RalieDownloadException("Falha ao baixar o novo arquivo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Encontra o arquivo RALIE mais recente no diretório de downloads
     */
    private Optional<Path> findLatestRalieFile() {
        try (var files = Files.list(downloadPath)
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().startsWith("ralie_"))
                .filter(p -> p.getFileName().toString().endsWith(".csv"))
                .sorted(Comparator.comparing(p -> p.toFile().lastModified(), Comparator.reverseOrder()))) {
            return files.findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
    
    private String getCsvFileUrl() {
        return RALIE_CSV_URL;
    }
    
    private void downloadFile(String fileUrl, Path targetPath) {
        log.debug("Iniciando download do arquivo de: {}", fileUrl);
        log.debug("Salvando em: {}", targetPath);
        
        try {
            Path tempFile = Files.createTempFile("ralie_download_", ".tmp");
            log.debug("Arquivo temporário criado: {}", tempFile);
            
            try {
                webClient.get()
                    .uri(fileUrl)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve()
                    .bodyToFlux(DataBuffer.class)
                    .map(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        return bytes;
                    })
                    .collectList()
                    .flatMap(bytesList -> {
                        byte[] allBytes = new byte[bytesList.stream().mapToInt(b -> b.length).sum()];
                        int offset = 0;
                        for (byte[] bytes : bytesList) {
                            System.arraycopy(bytes, 0, allBytes, offset, bytes.length);
                            offset += bytes.length;
                        }
                        return Mono.just(allBytes);
                    })
                    .doOnNext(bytes -> {
                        try {
                            Files.write(tempFile, bytes);
                        } catch (IOException e) {
                            log.error("Erro ao salvar o arquivo temporário: {}", e.getMessage(), e);
                            throw new RuntimeException("Erro ao salvar o arquivo temporário", e);
                        }
                    })
                    .block();
                
                Files.move(tempFile, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                log.debug("Arquivo movido para o destino final: {}", targetPath);
                
            } catch (Exception e) {
                log.warn("Erro durante o download. Removendo arquivo temporário: {}", tempFile);
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ex) {
                    log.warn("Não foi possível remover o arquivo temporário: {}", tempFile, ex);
                }
                throw e;
            }
            
        } catch (Exception e) {
            String errorMsg = String.format("Erro ao baixar o arquivo: %s", e.getMessage());
            log.error(errorMsg, e);
            throw new RalieDownloadException(errorMsg, e);
        }
    }
}
