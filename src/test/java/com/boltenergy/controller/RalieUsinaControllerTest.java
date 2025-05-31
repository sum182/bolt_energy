package com.boltenergy.controller;

import com.boltenergy.service.AneelRalieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link RalieUsinaController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RalieUsinaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AneelRalieService aneelRalieService;

    @Test
    void downloadRalieCsv_Success() throws Exception {
        // Arrange
        String csvContent = "id,nome,cnpj\n1,Usina Teste,12345678000199";
        String tempFilePath = System.getProperty("java.io.tmpdir") + "/ralie_20250101_120000.csv";
        
        when(aneelRalieService.downloadRalieCsv()).thenReturn(tempFilePath);
        
        // Act & Assert
        mockMvc.perform(get("/api/ralie-usina/download-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", 
                        "attachment; filename=ralie_20250101_120000.csv"));
    }

    @Test
    void downloadRalieCsv_FileNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(aneelRalieService.downloadRalieCsv()).thenReturn("/caminho/inexistente/arquivo.csv");
        
        // Act & Assert
        mockMvc.perform(get("/api/ralie-usina/download-csv"))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadRalieCsv_ServiceError_ReturnsInternalServerError() throws Exception {
        // Arrange
        when(aneelRalieService.downloadRalieCsv())
                .thenThrow(new RuntimeException("Erro ao baixar o arquivo"));
        
        // Act & Assert
        mockMvc.perform(get("/api/ralie-usina/download-csv"))
                .andExpect(status().isInternalServerError());
    }
}
