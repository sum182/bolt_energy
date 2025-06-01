package com.boltenergy.repository;

import com.boltenergy.model.entity.RalieUsinaEmpresaPotenciaGeradaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RalieUsinaEmpresaPotenciaGeradaRepository extends JpaRepository<RalieUsinaEmpresaPotenciaGeradaEntity, Long> {
    
    boolean existsByCodCeg(String codCeg);
    
    void deleteAllInBatch();
}
