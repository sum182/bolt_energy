package com.boltenergy.integration;

import com.boltenergy.service.AneelRalieService;
import com.boltenergy.service.RalieMetadataService;
import com.boltenergy.service.RalieUsinaCsvImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AneelRalieServiceIT {

    @InjectMocks
    private AneelRalieService aneelRalieService;
    
    @Mock
    private WebClient webClient;
    
    @Mock
    private RalieMetadataService metadataService;
    
    @Mock
    private RalieUsinaCsvImportService csvImportService;
    
    @Test
    void contextLoads() {
        // Teste simples para verificar se o contexto carrega corretamente
        assertNotNull(aneelRalieService, "O serviço deve ser injetado corretamente");
    }
}
