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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controller for Bolt Energy API tests.
 * Provides endpoints for API testing and validation.
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(
    name = "Test Controller",
    description = "Endpoints for API testing and validation"
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
     * Endpoint síncrono para buscar a página inicial do Google.
     * Retorna o HTML da página inicial do Google de forma síncrona.
     *
     * @return ResponseEntity com o conteúdo HTML da página do Google
     */
    @GetMapping(value = "/google", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(
        summary = "[Teste] Busca da página inicial do Google",
        description = "Endpoint de teste que retorna o HTML da página inicial do Google"
    )
    public ResponseEntity<String> getGoogleHomepage() {
        log.info("Recebida requisição para buscar página do Google");
        
        try {
            String html = googleService.fetchGoogleHomepage();
            log.info("Página do Google obtida com sucesso");
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
        } catch (Exception e) {
            log.error("Erro ao buscar página do Google: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Erro ao acessar o Google: " + e.getMessage());
        }
    }

    /**
     * Endpoint assíncrono para buscar a página inicial do Google.
     * Retorna o HTML da página inicial do Google de forma não-bloqueante.
     *
     * @return Mono com o ResponseEntity contendo o HTML da página do Google
     */
    @GetMapping(value = "/google/async", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(
        summary = "[Teste] Busca assíncrona da página inicial do Google",
        description = "Endpoint de teste que retorna o HTML da página inicial do Google de forma assíncrona"
    )
    public Mono<ResponseEntity<String>> getGoogleHomepageAsync() {
        log.info("Recebida requisição assíncrona para buscar página do Google");
        
        return googleService.fetchGoogleHomepageAsync()
                .map(html -> {
                    log.info("Página do Google obtida com sucesso (assíncrono)");
                    return ResponseEntity.ok()
                            .contentType(MediaType.TEXT_HTML)
                            .body(html);
                })
                .onErrorResume(e -> {
                    log.error("Erro ao buscar página do Google (assíncrono): {}", e.getMessage());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body("Erro ao acessar o Google: " + e.getMessage()));
                });
    }
}
