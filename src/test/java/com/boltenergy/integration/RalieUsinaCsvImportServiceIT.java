package com.boltenergy.integration;

import com.boltenergy.model.entity.RalieUsinaCsvImportEntity;
import com.boltenergy.repository.RalieUsinaCsvImportRepository;
import com.boltenergy.service.RalieUsinaCsvImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(RalieUsinaCsvImportService.class)
@Transactional
@Sql(scripts = "classpath:schema.sql")
class RalieUsinaCsvImportServiceIT {

    @Autowired
    private RalieUsinaCsvImportService csvImportService;
    
    @Autowired
    private RalieUsinaCsvImportRepository repository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private String testCsvContent;
    
    @BeforeEach
    void setUp() throws IOException {
        // Carrega o conteúdo do arquivo CSV de teste
        ClassPathResource resource = new ClassPathResource("test-data/ralie-test-data.csv");
        testCsvContent = new String(Files.readAllBytes(Paths.get(resource.getURI())));
        
        // Limpa os dados antes de cada teste
        repository.deleteAllInBatch();
        entityManager.flush();
        entityManager.clear();
    }
    
    @Test
    void importCsv_WithValidCsv_ShouldImportRecords() {
        try {
            // Act
            csvImportService.importCsv(testCsvContent);
            
            // Assert
            List<RalieUsinaCsvImportEntity> importedRecords = repository.findAll();
            assertEquals(2, importedRecords.size(), "Deveria ter importado 2 registros");
            
            // Verifica o primeiro registro
            RalieUsinaCsvImportEntity firstRecord = importedRecords.stream()
                .filter(r -> "UHE.PH.RS.000324-7.1".equals(r.getCodCeg()))
                .findFirst()
                .orElseThrow();
                
            assertEquals("Usina Teste 1", firstRecord.getNomEmpreendimento());
            assertEquals(10000.0, firstRecord.getMdaPotenciaOutorgadaKw(), 0.001);
            
            // Verifica o segundo registro
            RalieUsinaCsvImportEntity secondRecord = importedRecords.stream()
                .filter(r -> "UHE.PH.RS.000325-7.1".equals(r.getCodCeg()))
                .findFirst()
                .orElseThrow();
                
            assertEquals("Usina Teste 2", secondRecord.getNomEmpreendimento());
            assertEquals(20000.0, secondRecord.getMdaPotenciaOutorgadaKw(), 0.001);
        } catch (Exception e) {
            fail("Não deveria lançar exceção: " + e.getMessage(), e);
        }
    }
    
    @Test
    void importCsv_WithEmptyCsv_ShouldNotImportAnyRecords() {
        try {
            // Arrange
            String emptyCsv = "DatGeracaoConjuntoDados;DatRalie;IdeNucleoCEG;CodCEG\n";
            
            // Act
            csvImportService.importCsv(emptyCsv);
            
            // Assert
            List<RalieUsinaCsvImportEntity> importedRecords = repository.findAll();
            assertTrue(importedRecords.isEmpty(), "Não deveria ter importado nenhum registro");
        } catch (Exception e) {
            fail("Não deveria lançar exceção: " + e.getMessage(), e);
        }
    }
    
    @Test
    void importCsv_WithMalformedCsv_ShouldThrowException() {
        // Arrange
        String malformedCsv = "Coluna1;Coluna2\n" +
                            "Valor1;Valor2;Valor3\n"; // Número incorreto de colunas
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            csvImportService.importCsv(malformedCsv);
        }, "Deveria lançar exceção para CSV mal formado");
    }
}
