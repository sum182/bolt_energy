package com.boltenergy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de exemplo para demonstração da API da Bolt Energy.
 * Fornece endpoints básicos para teste e validação do ambiente.
 */
@Tag(
    name = "Hello Controller",
    description = "Endpoints de exemplo para demonstração da API"
)
@RestController
@RequestMapping("/api")
public class HelloController {
    
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);
    
    /**
     * Endpoint de exemplo que retorna uma mensagem de boas-vindas.
     * 
     * @return ResponseEntity com uma mensagem de boas-vindas
     */
    @Operation(
        summary = "Retorna uma mensagem de boas-vindas",
        description = "Endpoint de exemplo que retorna uma mensagem de boas-vindas da API",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Mensagem de boas-vindas retornada com sucesso",
                content = @Content(
                    mediaType = "text/plain",
                    examples = @ExampleObject("Bem-vindo à API da Bolt Energy!")
                )
            )
        }
    )
    @GetMapping(
        value = "/hello",
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> hello() {
        logger.info("Endpoint /api/hello acessado");
        return ResponseEntity.ok("Bem-vindo à API da Bolt Energy!");
    }
}
