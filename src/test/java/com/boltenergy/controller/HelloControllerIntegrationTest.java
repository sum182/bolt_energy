package com.boltenergy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de integração para o HelloController.
 * 
 * <p>Este teste verifica se o endpoint /api/hello está funcionando corretamente.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Testa o endpoint /api/hello para garantir que retorne a mensagem de boas-vindas.
     * 
     * @throws Exception se ocorrer um erro durante a execução do teste
     */
    @Test
    void hello_ShouldReturnWelcomeMessage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(containsString("Bem-vindo à API da Bolt Energy!")));
    }

    /**
     * Testa o endpoint /api/hello para garantir que não aceite outros métodos HTTP além de GET.
     * 
     * @throws Exception se ocorrer um erro durante a execução do teste
     */
    @Test
    void hello_ShouldNotAcceptPostRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/hello").with(request -> {
                    request.setMethod("POST");
                    return request;
                }))
                .andExpect(status().isMethodNotAllowed());
    }
}
