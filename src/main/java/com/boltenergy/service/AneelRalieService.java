package com.boltenergy.service;

import com.boltenergy.config.WebClientConfig;
import com.boltenergy.exception.RalieDownloadException;
import com.boltenergy.model.RalieMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;
    private WebClient webClient;
    private Path appBasePath;
    private Path downloadPath;
    private RalieMetadata metadata;
    
    @PostConstruct
    public void init() {
        this.webClient = webClientConfig.createWebClient(ANEEL_BASE_URL);
        
        try {
            // Obtém o diretório raiz da aplicação
            this.appBasePath = Paths.get("").toAbsolutePath();
            log.info("Diretório raiz da aplicação: {}", this.appBasePath);
            
            // Cria o diretório de downloads se não existir
            this.downloadPath = appBasePath.resolve(DOWNLOAD_DIR);
            if (!Files.exists(this.downloadPath)) {
                Files.createDirectories(this.downloadPath);
                log.info("Diretório de downloads criado em: {}", this.downloadPath);
            } else {
                log.info("Usando diretório de downloads existente: {}", this.downloadPath);
            }
            
            // Verifica permissão de escrita
            if (!Files.isWritable(this.downloadPath)) {
                throw new IOException("Sem permissão de escrita no diretório: " + this.downloadPath);
            }
            
            // Inicializa o serviço de metadados no diretório de downloads
            metadataService.init(this.downloadPath);
            this.metadata = metadataService.loadMetadata();
            log.info("Metadados carregados. Último download em: {}", 
                    metadata.getFormattedLastDownloadTime());
            log.info("Arquivo de metadados: {}", this.downloadPath.resolve("ralie_metadata.json"));
                    
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
            
            // Verifica se o arquivo remoto foi modificado
            if (!hasRemoteFileChanged(csvUrl)) {
                log.info("O arquivo remoto não foi modificado desde o último download");
                return findLatestRalieFile()
                    .map(Path::toString)
                    .orElseGet(() -> downloadNewFile(csvUrl));
            }
            
            // Se o arquivo foi modificado ou é o primeiro download
            log.info("Arquivo remoto foi modificado ou é o primeiro download");
            return downloadNewFile(csvUrl);
            
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
     * Verifica se o arquivo remoto foi modificado desde o último download
     */
    private boolean hasRemoteFileChanged(String fileUrl) {
        try {
            // Se não temos metadados salvos, considera que o arquivo foi modificado
            if (metadata.getEtag() == null && metadata.getLastModified() == null) {
                log.info("Nenhum ETag ou Last-Modified anterior encontrado, considerando como modificado");
                return true;
            }
            
            HttpHeaders headers;
            try {
                // Faz uma requisição HEAD para verificar os cabeçalhos
                headers = webClient.head()
                    .uri(fileUrl)
                    .retrieve()
                    .toBodilessEntity()
                    .block()
                    .getHeaders();
            } catch (Exception e) {
                log.warn("Erro ao verificar cabeçalhos do arquivo remoto: {}. Considerando como modificado.", e.getMessage());
                return true;
            }
            
            String currentEtag = headers.getETag();
            String currentLastModified = headers.getFirst("Last-Modified");
            
            // Se o servidor não suporta ETag nem Last-Modified, considera que o arquivo foi modificado
            if (currentEtag == null && currentLastModified == null) {
                log.info("Servidor não retornou ETag nem Last-Modified, considerando como modificado");
                return true;
            }
            
            // Se temos um ETag salvo e o servidor retornou um ETag, verifica se mudou
            if (metadata.getEtag() != null && currentEtag != null) {
                boolean etagChanged = !currentEtag.equals(metadata.getEtag());
                log.info("Comparação de ETag - Antigo: {}, Novo: {}, Modificado: {}", 
                        metadata.getEtag(), currentEtag, etagChanged);
                if (etagChanged) {
                    return true;
                }
                // Se o ETag é o mesmo, o arquivo não mudou
                return false;
            }
            
            // Se chegou aqui, ou não temos ETag ou o servidor não retornou ETag
            // Verifica o Last-Modified se disponível
            if (metadata.getLastModified() != null && currentLastModified != null) {
                boolean lastModifiedChanged = !currentLastModified.equals(metadata.getLastModified());
                log.info("Comparação de Last-Modified - Antigo: {}, Novo: {}, Modificado: {}", 
                        metadata.getLastModified(), currentLastModified, lastModifiedChanged);
                return lastModifiedChanged;
            }
            
            // Se não foi possível verificar por nenhum método, assume que o arquivo foi modificado
            log.info("Não foi possível determinar se o arquivo foi modificado. Considerando como modificado.");
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao verificar alterações no arquivo remoto: {}", e.getMessage(), e);
            // Em caso de erro, assume que o arquivo foi modificado para garantir o download
            return true;
        }
    }
    
    /**
     * Baixa um novo arquivo e atualiza os metadados
     */
    private String downloadNewFile(String fileUrl) {
        try {
            // Primeiro faz uma requisição HEAD para obter os cabeçalhos
            ResponseEntity<Void> headResponse = webClient.head()
                .uri(fileUrl)
                .retrieve()
                .toBodilessEntity()
                .block();
                
            // Obtém os cabeçalhos da resposta
            HttpHeaders responseHeaders = headResponse.getHeaders();
            String etag = responseHeaders.getETag();
            String lastModified = responseHeaders.getFirst("Last-Modified");
            
            log.info("Novos cabeçalhos recebidos - ETag: {}, Last-Modified: {}", etag, lastModified);
            
            // Gera um nome de arquivo com timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("ralie_%s.csv", timestamp);
            Path filePath = downloadPath.resolve(fileName);
            
            // Faz o download do arquivo
            downloadFile(fileUrl, filePath);
            
            // Atualiza os metadados
            long fileSize = Files.size(filePath);
            metadata.update(etag, lastModified, filePath.toString(), fileSize);
            metadataService.saveMetadata(metadata);
            
            log.info("Novo arquivo RALIE baixado com sucesso para: {} (Tamanho: {} bytes)", 
                    filePath, fileSize);
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
        try {
            // Lista todos os arquivos CSV no diretório de downloads
            try (var files = Files.list(downloadPath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith("ralie_"))
                    .filter(p -> p.getFileName().toString().endsWith(".csv"))
                    .sorted(Comparator.comparing(p -> p.toFile().lastModified(), Comparator.reverseOrder()))) {
                
                return files.findFirst();
            }
        } catch (IOException e) {
            log.error("Erro ao buscar arquivos RALIE: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    private String getCsvFileUrl() {
        try {
            log.info("Obtendo URL do arquivo CSV do RALIE");
            // Retorna a URL direta para o arquivo CSV
            return RALIE_CSV_URL;
            
        } catch (Exception e) {
            String errorMsg = String.format("Erro ao obter a URL do arquivo CSV: %s", e.getMessage());
            log.error(errorMsg, e);
            throw new RalieDownloadException(errorMsg, e);
        }
    }
    
    /**
     * Downloads a file from the given URL and saves it to the specified path.
     * 
     * @param fileUrl URL do arquivo a ser baixado
     * @param targetPath Caminho onde o arquivo será salvo
     */
    private void downloadFile(String fileUrl, Path targetPath) {
        log.info("Iniciando download do arquivo de: {}", fileUrl);
        log.info("Salvando em: {}", targetPath);
        
        try {
            long startTime = System.currentTimeMillis();
            Path tempFile = Files.createTempFile("ralie_download_", ".tmp");
            
            try {
                // Baixa o arquivo para um arquivo temporário primeiro
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
                        try {
                            byte[] allBytes = new byte[bytesList.stream().mapToInt(b -> b.length).sum()];
                            int offset = 0;
                            for (byte[] bytes : bytesList) {
                                System.arraycopy(bytes, 0, allBytes, offset, bytes.length);
                                offset += bytes.length;
                            }
                            return Mono.just(allBytes);
                        } catch (Exception e) {
                            return Mono.error(e);
                        }
                    })
                    .doOnNext(bytes -> {
                        try {
                            Files.write(tempFile, bytes);
                        } catch (IOException e) {
                            throw new RuntimeException("Erro ao salvar o arquivo temporário", e);
                        }
                    })
                    .block();
                
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
                    Files.deleteIfExists(tempFile);
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
