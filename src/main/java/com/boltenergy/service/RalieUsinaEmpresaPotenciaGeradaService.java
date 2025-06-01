package com.boltenergy.service;

import com.boltenergy.model.entity.RalieUsinaEmpresaPotenciaGeradaEntity;
import com.boltenergy.repository.RalieUsinaCsvImportRepository;
import com.boltenergy.repository.RalieUsinaEmpresaPotenciaGeradaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RalieUsinaEmpresaPotenciaGeradaService {
    private final RalieUsinaEmpresaPotenciaGeradaRepository repository;
    private final RalieUsinaCsvImportRepository csvImportRepository;
    
    @Transactional
    public void processImportedData() {
        log.info("Iniciando processamento dos dados importados para a tabela de potência gerada");
        
        long totalRecords = csvImportRepository.count();
        log.info("Total de registros na tabela de importação: {}", totalRecords);
        
        if (totalRecords == 0) {
            log.warn("Nenhum registro encontrado na tabela de importação");
            return;
        }
        
        try {
            deleteAll();
            log.info("Iniciando processamento em lote no banco de dados...");
            repository.processDataTableLargestGenerators();
            
            long processedRecords = repository.count();
            log.info("Processamento concluído. {} geradoras processadas.", processedRecords);
            
        } catch (Exception e) {
            log.error("Erro ao processar dados para a tabela de potência gerada", e);
            throw new RuntimeException("Falha ao processar dados de potência gerada", e);
        }
    }
    
    @Transactional
    public void saveAll(List<RalieUsinaEmpresaPotenciaGeradaEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            log.warn("Nenhum registro para salvar");
            return;
        }
        
        repository.saveAll(entities);
        log.info("{} registros salvos na tabela de potência gerada", entities.size());
    }
    
    @Transactional
    public void deleteAll() {
        log.info("Removendo todos os registros de potência gerada");
        if (repository.count() > 0) {
            repository.deleteAllInBatch();
            log.info("Todos os registros de potência gerada foram removidos");
        } else {
            log.info("Nenhum registro de potência gerada encontrado para remoção");
        }
    }
    
    @Transactional(readOnly = true)
    public List<RalieUsinaEmpresaPotenciaGeradaEntity> findAll() {
        return repository.findAll();
    }
    
    @Transactional(readOnly = true)
    public boolean existsByCodCeg(String codCeg) {
        return repository.existsByCodCeg(codCeg);
    }
    
    @Transactional(readOnly = true)
    public List<RalieUsinaEmpresaPotenciaGeradaEntity> findTop5LargestGenerators() {
        log.info("Buscando as 5 maiores geradoras de energia");
        return repository.findTop5ByOrderByPotenciaDesc();
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void processLargestGeneratorsTable() {
        log.info("Iniciando processamento da tabela de maiores geradoras");
        
        log.info("Limpando tabela de potência gerada");
        repository.deleteAll();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Processando novos dados de potência gerada");
        repository.processDataTableLargestGenerators();
        
        log.info("Processamento da tabela de maiores geradoras concluído com sucesso");
    }
}
