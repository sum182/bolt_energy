package com.boltenergy.controller;

import com.boltenergy.service.GoogleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller para testes da API da Bolt Energy.
 * Fornece endpoints para teste e validação da API.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
@Tag(
    name = "Test Controller",
    description = "Endpoints para teste e validação da API"
)
public class TestController {

    private final GoogleService googleService;

    /**
     * Endpoint de teste que retorna uma mensagem de boas-vindas.
     * 
     * @return ResponseEntity com uma mensagem de boas-vindas
     */
    @Operation(
        summary = "Retorna uma mensagem de boas-vindas de teste",
        description = "Endpoint de teste que retorna uma mensagem de boas-vindas da API",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Mensagem de boas-vindas retornada com sucesso",
                content = @Content(
                    mediaType = "text/plain",
                    examples = @ExampleObject("Bem-vindo ao endpoint de teste da Bolt Energy!")
                )
            )
        }
    )
    @GetMapping(
        value = "/hello",
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> hello() {
        log.info("Endpoint /api/test/hello acessado");
        return ResponseEntity.ok("Bem-vindo ao endpoint de teste da Bolt Energy!");
    }

    /**
     * Endpoint para buscar a página inicial do Google (para teste).
     * Retorna o HTML da página inicial do Google.
     *
     * @return ResponseEntity com o conteúdo HTML da página do Google
     */
    @GetMapping(value = "/google-homepage", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(
        summary = "[Teste] Busca a página inicial do Google",
        description = "Endpoint de teste que retorna o HTML da página inicial do Google"
    )
    public Mono<ResponseEntity<String>> getGoogleHomepage() {
        log.info("[TEST] Received request to fetch Google homepage from test endpoint");
        
        return googleService.fetchGoogleHomepage()
                .map(html -> {
                    log.info("[TEST] Successfully returning Google homepage");
                    return ResponseEntity.ok()
                            .contentType(MediaType.TEXT_HTML)
                            .body(html);
                })
                .onErrorResume(e -> {
                    log.error("[TEST] Error fetching Google homepage: {}", e.getMessage());
                    String errorMessage = "[TEST] Erro ao acessar o Google: " + e.getMessage();
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(errorMessage));
                });
    }
}
