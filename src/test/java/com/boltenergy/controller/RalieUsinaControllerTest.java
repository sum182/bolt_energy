package com.boltenergy.controller;

import com.boltenergy.service.AneelRalieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RalieUsinaControllerTest {

    private static final String DOWNLOAD_CSV_ENDPOINT = "/api/ralie-usina/download-csv";
    private static final String FILE_NOT_MODIFIED_MSG = "O arquivo remoto não foi modificado desde o último download";
    private static final String DOWNLOAD_SUCCESS_MSG_PREFIX = "Novo arquivo RALIE baixado com sucesso:";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AneelRalieService aneelRalieService;

    @Test
    void downloadRalieCsv_WhenFileDownloaded_ReturnsSuccessMessage() throws Exception {
        Path tempFile = createTempFile();
        when(aneelRalieService.downloadRalieCsv()).thenReturn(tempFile.toString());
        
        String response = mockMvc.perform(get(DOWNLOAD_CSV_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
                
        assertTrue(response.startsWith(DOWNLOAD_SUCCESS_MSG_PREFIX) || 
                 response.equals(FILE_NOT_MODIFIED_MSG));
    }

    @Test
    void downloadRalieCsv_WhenFileNotModified_ReturnsNotModifiedMessage() throws Exception {
        when(aneelRalieService.downloadRalieCsv()).thenReturn("caminho/arquivo.csv");
        
        mockMvc.perform(get(DOWNLOAD_CSV_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(content().string(FILE_NOT_MODIFIED_MSG));
    }

    @Test
    void downloadRalieCsv_WhenServiceThrowsException_ReturnsInternalServerError() throws Exception {
        when(aneelRalieService.downloadRalieCsv())
                .thenThrow(new RuntimeException("Erro ao baixar o arquivo"));
        
        mockMvc.perform(get(DOWNLOAD_CSV_ENDPOINT))
                .andExpect(status().isInternalServerError());
    }

    private Path createTempFile() throws Exception {
        Path tempFile = Files.createTempFile("ralie_", ".csv");
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }
}
