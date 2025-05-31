package com.boltenergy.service;

import com.boltenergy.config.WebClientConfig;
import com.boltenergy.exception.RalieDownloadException;
import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
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
import java.time.format.DateTimeFormatter;

/**
 * Service for downloading and processing RALIE data from ANEEL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AneelRalieService {

    private static final String ANEEL_BASE_URL = "https://dadosabertos.aneel.gov.br";
    // URL direta para o arquivo CSV do RALIE (atualizada para o recurso correto)
    private static final String RALIE_CSV_URL = "https://dadosabertos.aneel.gov.br/dataset/57e4b8b5-a5db-40e6-9901-27ca629d0477/resource/4a615df8-4c25-48fa-bbea-873a36a79518/download/ralie-usina.csv";

    private static final String DOWNLOAD_DIR = "downloads";
    
    private final WebClientConfig webClientConfig;
    private WebClient webClient;
    private Path appBasePath;
    
    @PostConstruct
    public void init() {
        this.webClient = webClientConfig.createWebClient(ANEEL_BASE_URL);
        
        try {
            // Obtém o diretório raiz da aplicação
            this.appBasePath = Paths.get("").toAbsolutePath();
            
            // Cria o diretório de downloads se não existir
            Path downloadPath = appBasePath.resolve(DOWNLOAD_DIR);
            if (!downloadPath.toFile().exists()) {
                Files.createDirectories(downloadPath);
                log.info("Diretório de downloads criado em: {}", downloadPath);
            }
        } catch (IOException e) {
            log.error("Erro ao configurar o diretório de downloads", e);
            throw new RalieDownloadException("Falha ao configurar o diretório de downloads", e);
        }
    }
    
    /**
     * Downloads the RALIE CSV file from ANEEL's open data portal.
     * 
     * @return Path to the downloaded file
     */
    public String downloadRalieCsv() {
        log.info("Iniciando download do arquivo RALIE da ANEEL");
        
        try {
            // Get the CSV file URL
            String csvUrl = getCsvFileUrl();
            log.info("URL do arquivo CSV: {}", csvUrl);
            
            // Generate a unique filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("ralie_%s.csv", timestamp);
            
            // Define o caminho do arquivo dentro do diretório de downloads
            Path downloadPath = appBasePath.resolve(DOWNLOAD_DIR).resolve(fileName);
            
            // Verifica se o diretório de downloads existe
            if (!Files.exists(downloadPath.getParent())) {
                Files.createDirectories(downloadPath.getParent());
                log.info("Diretório de downloads criado em: {}", downloadPath.getParent());
            }
            
            // Download and save the file
            downloadFile(csvUrl, downloadPath);
            
            log.info("Arquivo RALIE baixado com sucesso para: {}", downloadPath);
            return downloadPath.toString();
            
        } catch (RalieDownloadException e) {
            // Relança exceções específicas do domínio
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format("Erro inesperado ao baixar o arquivo RALIE: %s", e.getMessage());
            log.error(errorMsg, e);
            throw new RalieDownloadException(errorMsg, e);
        }
    }
    
    /**
     * Gets the CSV file URL from ANEEL's API.
     * 
     * @return URL do arquivo CSV do RALIE
     */
    private String getCsvFileUrl() {
        try {
            log.info("Obtendo URL do arquivo CSV do RALIE");
            // Retorna a URL direta para o arquivo CSV
            return RALIE_CSV_URL;
        } catch (Exception e) {
            log.error("Erro ao obter a URL do arquivo CSV: {}", e.getMessage(), e);
            throw new RalieDownloadException("Falha ao obter a URL do arquivo CSV: " + e.getMessage(), e);
        }
    }
    
    /**
     * Downloads a file from the given URL and saves it to the specified path.
     * 
     * @param fileUrl URL do arquivo a ser baixado
     * @param targetPath Caminho onde o arquivo será salvo
     */
    private void downloadFile(String fileUrl, Path targetPath) {
        log.info("Iniciando download do arquivo: {}", fileUrl);
        long startTime = System.currentTimeMillis();
        
        try {
            // Garante que o diretório de destino existe
            Files.createDirectories(targetPath.getParent());
            
            // Cria um arquivo temporário primeiro
            Path tempFile = Files.createTempFile(targetPath.getParent(), "temp_", ".tmp");
            
            try {
                log.debug("Criado arquivo temporário: {}", tempFile);
                
                // Cria um fluxo reativo para processar o download em blocos
                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<Throwable> errorRef = new AtomicReference<>();
                
                // Inicia o download assíncrono
                Disposable disposable = webClient.get()
                        .uri(fileUrl)
                        .accept(MediaType.APPLICATION_OCTET_STREAM)
                        .retrieve()
                        .bodyToFlux(DataBuffer.class)
                        .doOnNext(dataBuffer -> {
                            try {
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                Files.write(tempFile, bytes, 
                                    java.nio.file.StandardOpenOption.APPEND, 
                                    java.nio.file.StandardOpenOption.CREATE);
                            } catch (IOException e) {
                                throw new RuntimeException("Erro ao escrever no arquivo temporário", e);
                            } finally {
                                org.springframework.core.io.buffer.DataBufferUtils.release(dataBuffer);
                            }
                        })
                        .doOnError(error -> {
                            log.error("Erro durante o download do arquivo", error);
                            errorRef.set(error);
                            latch.countDown();
                        })
                        .doOnComplete(() -> {
                            log.debug("Download concluído com sucesso para o arquivo temporário");
                            latch.countDown();
                        })
                        .subscribe();
                
                try {
                    // Aguarda o término do download ou erro
                    boolean completed = latch.await(10, TimeUnit.MINUTES);
                    if (!completed) {
                        disposable.dispose(); // Cancela o download se estiver demorando muito
                        throw new RalieDownloadException("Tempo limite excedido ao baixar o arquivo");
                    }
                    
                    // Verifica se houve erro durante o download
                    if (errorRef.get() != null) {
                        throw new RalieDownloadException("Falha ao baixar o arquivo: " + errorRef.get().getMessage(), errorRef.get());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    disposable.dispose();
                    throw new RalieDownloadException("Download interrompido", e);
                }
                
                // Move o arquivo temporário para o destino final
                Files.move(tempFile, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                long fileSize = Files.size(targetPath);
                double duration = (System.currentTimeMillis() - startTime) / 1000.0;
                
                log.info("Download concluído em {}s - Tamanho: {}MB", 
                        String.format("%.2f", duration), 
                        String.format("%.2f", fileSize / (1024.0 * 1024.0)));
                
            } catch (Exception e) {
                // Tenta excluir o arquivo temporário em caso de erro
                try {
                    if (Files.exists(tempFile)) {
                        Files.deleteIfExists(tempFile);
                    }
                } catch (IOException ex) {
                    log.warn("Não foi possível excluir o arquivo temporário: {}", tempFile, ex);
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
