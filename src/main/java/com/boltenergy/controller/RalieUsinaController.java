package com.boltenergy.controller;

import com.boltenergy.service.AneelRalieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * Controller for handling RALIE (Relatório de Acompanhamento da Expansão da Oferta de Geração de Energia Elétrica) operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/ralie-usina")
@RequiredArgsConstructor
@Tag(
    name = "RALIE Usina Controller",
    description = "Endpoints para operações relacionadas aos dados RALIE de usinas da ANEEL"
)
public class RalieUsinaController {

    private final AneelRalieService aneelRalieService;

    /**
     * Downloads the latest RALIE CSV file from ANEEL's open data portal.
     *
     * @return ResponseEntity containing the CSV file
     */
    @GetMapping("/download-csv")
    @Operation(
        summary = "Baixar arquivo CSV do RALIE",
        description = "Faz o download do arquivo CSV mais recente do Relatório de Acompanhamento da Expansão da Oferta de Geração de Energia Elétrica (RALIE) da ANEEL"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Arquivo CSV baixado com sucesso",
        content = @Content(mediaType = "text/csv")
    )
    @ApiResponse(
        responseCode = "500",
        description = "Erro ao processar a requisição"
    )
    public ResponseEntity<FileSystemResource> downloadRalieCsv() {
        log.info("Recebida requisição para download do arquivo RALIE");
        
        // Download the CSV file
        String filePath = aneelRalieService.downloadRalieCsv();
        File file = new File(filePath);
        
        if (!file.exists()) {
            log.error("Arquivo não encontrado após o download: {}", filePath);
            return ResponseEntity.notFound().build();
        }
        
        // Set up response headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        
        log.info("Arquivo RALIE disponibilizado para download: {}", filePath);
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new FileSystemResource(file));
    }
}
