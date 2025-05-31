package com.boltenergy.controller;

import com.boltenergy.service.GoogleService;
import io.swagger.v3.oas.annotations.Operation;
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
 * Controller for Google-related endpoints.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/google")
@Tag(name = "Google", description = "Endpoints para integração com o Google")
public class GoogleController {

    private final GoogleService googleService;

    /**
     * Endpoint para buscar a página inicial do Google.
     * Retorna o HTML da página inicial do Google.
     *
     * @return ResponseEntity com o conteúdo HTML da página do Google
     */
    @GetMapping(value = "/homepage", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(
        summary = "Busca a página inicial do Google",
        description = "Retorna o HTML da página inicial do Google"
    )
    public Mono<ResponseEntity<String>> getGoogleHomepage() {
        log.info("Received request to fetch Google homepage");
        
        return googleService.fetchGoogleHomepage()
                .map(html -> {
                    log.info("Successfully returning Google homepage");
                    return ResponseEntity.ok()
                            .contentType(MediaType.TEXT_HTML)
                            .body(html);
                })
                .onErrorResume(e -> {
                    log.error("Error fetching Google homepage: {}", e.getMessage());
                    String errorMessage = "Erro ao acessar o Google: " + e.getMessage();
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(errorMessage));
                });
    }
}
