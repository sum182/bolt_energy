package com.boltenergy.controller;

import com.boltenergy.model.dto.RalieUsinaEmpresaPotenciaGeradaDTO;
import com.boltenergy.service.AneelRalieService;
import com.boltenergy.service.RalieUsinaEmpresaPotenciaGeradaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

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
    private final RalieUsinaEmpresaPotenciaGeradaService potenciaGeradaService;

    @GetMapping("/download-csv")
    @Operation(
        summary = "Baixar arquivo CSV do RALIE",
        description = "Faz o download do arquivo CSV mais recente do Relatório de Acompanhamento da Expansão da Oferta de Geração de Energia Elétrica (RALIE) da ANEEL"
    )
    @ApiResponse(responseCode = "200", description = "Arquivo CSV baixado com sucesso")
    @ApiResponse(responseCode = "500", description = "Erro ao processar a requisição")
    public ResponseEntity<String> downloadRalieCsv() {
        log.info("Recebida requisição para download do arquivo RALIE");
        
        long beforeDownload = System.currentTimeMillis();
        String filePath = aneelRalieService.downloadRalieCsv();
        
        File file = new File(filePath);
        if (file.lastModified() >= beforeDownload) {
            log.info("Novo arquivo RALIE baixado com sucesso: {}", filePath);
            return ResponseEntity.ok("Novo arquivo RALIE baixado com sucesso: " + filePath);
        }
        
        log.info("Utilizando arquivo RALIE existente: {}", filePath);
        return ResponseEntity.ok("Utilizando arquivo RALIE existente: " + filePath);
    }
    
    @GetMapping("/maiores-geradoras")
    @Operation(
        summary = "Listar as 5 maiores geradoras de energia",
        description = "Retorna as 5 geradoras com as maiores potências totais, ordenadas de forma decrescente"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Lista das 5 maiores geradoras retornada com sucesso",
        content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = RalieUsinaEmpresaPotenciaGeradaDTO.class))
        )
    )
    public ResponseEntity<List<RalieUsinaEmpresaPotenciaGeradaDTO>> listarMaioresGeradoras() {
        log.info("Recebida requisição para listar as 5 maiores geradoras de energia");
        
        var generators = potenciaGeradaService.findTop5LargestGenerators().stream()
            .map(RalieUsinaEmpresaPotenciaGeradaDTO::fromEntity)
            .collect(Collectors.toList());
            
        log.info("Retornando as 5 maiores geradoras de energia");
        return ResponseEntity.ok(generators);
    }
}
