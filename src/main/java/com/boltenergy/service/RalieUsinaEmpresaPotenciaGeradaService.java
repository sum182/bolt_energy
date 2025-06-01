package com.boltenergy.service;

import com.boltenergy.model.entity.RalieUsinaCsvImportEntity;
import com.boltenergy.model.entity.RalieUsinaEmpresaPotenciaGeradaEntity;
import com.boltenergy.repository.RalieUsinaCsvImportRepository;
import com.boltenergy.repository.RalieUsinaEmpresaPotenciaGeradaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RalieUsinaEmpresaPotenciaGeradaService {
    private final RalieUsinaEmpresaPotenciaGeradaRepository repository;
    private final RalieUsinaCsvImportRepository csvImportRepository;
    
    @Transactional
    public void processarDadosImportados() {
        log.info("Iniciando processamento dos dados importados para a tabela de potência gerada");
        
        List<RalieUsinaCsvImportEntity> registros = csvImportRepository.findAll();
        log.info("Total de registros encontrados na tabela de importação: {}", registros.size());
        
        if (registros.isEmpty()) {
            log.warn("Nenhum registro encontrado na tabela de importação");
            return;
        }
        
        // Agrupa por CodCEG e soma as potências
        Map<String, RalieUsinaEmpresaPotenciaGeradaEntity> empreendimentosMap = new HashMap<>();
        int registrosSemCodCeg = 0;
        
        for (RalieUsinaCsvImportEntity registro : registros) {
            if (registro.getCodCeg() == null || registro.getCodCeg().trim().isEmpty()) {
                registrosSemCodCeg++;
                log.debug("Registro ignorado - CodCEG ausente ou inválido: {}", registro);
                continue;
            }
            
            String codCeg = registro.getCodCeg().trim();
            String nomeEmpreendimento = (registro.getNomEmpreendimento() != null && !registro.getNomEmpreendimento().trim().isEmpty()) 
                ? registro.getNomEmpreendimento().trim() 
                : "Empreendimento " + codCeg;
                
            Double potencia = (registro.getMdaPotenciaOutorgadaKw() != null) 
                ? registro.getMdaPotenciaOutorgadaKw() 
                : 0.0;
            
            empreendimentosMap.compute(codCeg, (key, existing) -> {
                if (existing == null) {
                    return RalieUsinaEmpresaPotenciaGeradaEntity.builder()
                        .codCeg(codCeg)
                        .nomEmpreendimento(nomeEmpreendimento)
                        .potencia(potencia)
                        .build();
                } else {
                    existing.setPotencia(existing.getPotencia() + potencia);
                    return existing;
                }
            });
        }
        
        if (!empreendimentosMap.isEmpty()) {
            // Remove registros existentes
            deleteAll();
            
            // Salva os novos registros
            List<RalieUsinaEmpresaPotenciaGeradaEntity> empreendimentosToSave = new ArrayList<>(empreendimentosMap.values());
            saveAll(empreendimentosToSave);
            
            log.info("Processamento concluído. {} empreendimentos processados. {} registros ignorados por falta de CodCEG.", 
                empreendimentosToSave.size(), registrosSemCodCeg);
        } else {
            log.warn("Nenhum registro válido encontrado para processamento");
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
    public List<RalieUsinaEmpresaPotenciaGeradaEntity> findTop5MaioresGeradores() {
        log.info("Buscando os 5 maiores geradores de energia");
        return repository.findTop5ByOrderByPotenciaDesc();
    }
}
