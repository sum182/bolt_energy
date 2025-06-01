package com.boltenergy.service;

import com.boltenergy.model.entity.RalieUsinaEmpresaPotenciaGeradaEntity;
import com.boltenergy.repository.RalieUsinaCsvImportRepository;
import com.boltenergy.repository.RalieUsinaEmpresaPotenciaGeradaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RalieUsinaEmpresaPotenciaGeradaServiceTest {

    @Mock
    private RalieUsinaEmpresaPotenciaGeradaRepository repository;

    @Mock
    private RalieUsinaCsvImportRepository csvImportRepository;

    @InjectMocks
    private RalieUsinaEmpresaPotenciaGeradaService service;

    private RalieUsinaEmpresaPotenciaGeradaEntity geradora1;
    private RalieUsinaEmpresaPotenciaGeradaEntity geradora2;
    private List<RalieUsinaEmpresaPotenciaGeradaEntity> geradoras;

    @BeforeEach
    void setUp() {
        geradora1 = RalieUsinaEmpresaPotenciaGeradaEntity.builder()
            .id(1L)
            .codCeg("UHE.PH.RS.000324-7.1")
            .nomEmpreendimento("Usina Teste 1")
            .potencia(new BigDecimal("10000.00"))
            .build();

        geradora2 = RalieUsinaEmpresaPotenciaGeradaEntity.builder()
            .id(2L)
            .codCeg("UHE.PH.RS.000325-7.1")
            .nomEmpreendimento("Usina Teste 2")
            .potencia(new BigDecimal("20000.00"))
            .build();

        geradoras = Arrays.asList(geradora1, geradora2);
    }

    @Test
    void processImportedData_WhenRecordsExist_ShouldProcessSuccessfully() {
        when(csvImportRepository.count()).thenReturn(10L);
        when(repository.count()).thenReturn(2L);
        
        service.processImportedData();
        
        verify(repository, times(1)).deleteAllInBatch();
        verify(repository, times(1)).processDataTableLargestGenerators();
        verify(csvImportRepository, times(1)).count();
        // O count() pode ser chamado mais de uma vez, então usamos atLeastOnce()
        verify(repository, atLeastOnce()).count();
    }

    @Test
    void processImportedData_WhenNoRecords_ShouldNotProcess() {
        when(csvImportRepository.count()).thenReturn(0L);
        
        service.processImportedData();
        
        verify(repository, never()).deleteAllInBatch();
        verify(repository, never()).processDataTableLargestGenerators();
        verify(csvImportRepository, times(1)).count();
    }

    @Test
    void saveAll_WithValidEntities_ShouldSaveSuccessfully() {
        service.saveAll(geradoras);
        
        verify(repository, times(1)).saveAll(geradoras);
    }

    @Test
    void saveAll_WithEmptyList_ShouldNotSave() {
        service.saveAll(Collections.emptyList());
        
        verify(repository, never()).saveAll(any());
    }

    @Test
    void deleteAll_WhenRecordsExist_ShouldDeleteAll() {
        when(repository.count()).thenReturn(2L);
        
        service.deleteAll();
        
        verify(repository, times(1)).deleteAllInBatch();
    }

    @Test
    void deleteAll_WhenNoRecords_ShouldNotDelete() {
        when(repository.count()).thenReturn(0L);
        
        service.deleteAll();
        
        verify(repository, never()).deleteAllInBatch();
    }

    @Test
    void findAll_ShouldReturnAllEntities() {
        when(repository.findAll()).thenReturn(geradoras);
        
        List<RalieUsinaEmpresaPotenciaGeradaEntity> result = service.findAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void existsByCodCeg_WhenExists_ShouldReturnTrue() {
        String codCeg = "UHE.PH.RS.000324-7.1";
        when(repository.existsByCodCeg(codCeg)).thenReturn(true);
        
        boolean exists = service.existsByCodCeg(codCeg);
        
        assertTrue(exists);
        verify(repository, times(1)).existsByCodCeg(codCeg);
    }

    @Test
    void findTop5LargestGenerators_ShouldReturnOrderedList() {
        when(repository.findTop5ByOrderByPotenciaDesc()).thenReturn(geradoras);
        
        List<RalieUsinaEmpresaPotenciaGeradaEntity> result = service.findTop5LargestGenerators();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findTop5ByOrderByPotenciaDesc();
    }

    @Test
    void processLargestGeneratorsTable_ShouldProcessSuccessfully() {
        service.processLargestGeneratorsTable();
        
        verify(repository, times(1)).deleteAll();
        verify(repository, times(1)).processDataTableLargestGenerators();
    }
}
