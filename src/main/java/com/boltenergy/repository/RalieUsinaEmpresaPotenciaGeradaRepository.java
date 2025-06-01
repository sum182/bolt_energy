package com.boltenergy.repository;

import com.boltenergy.model.entity.RalieUsinaEmpresaPotenciaGeradaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RalieUsinaEmpresaPotenciaGeradaRepository extends JpaRepository<RalieUsinaEmpresaPotenciaGeradaEntity, Long> {

    boolean existsByCodCeg(String codCeg);

    void deleteAllInBatch();

    List<RalieUsinaEmpresaPotenciaGeradaEntity> findTop5ByOrderByPotenciaDesc();

    @Query(nativeQuery = true, value =
        "INSERT INTO ralie_usina_empresa_potencia_gerada (cod_ceg, nom_empreendimento, potencia) " +
            "SELECT " +
            "    cod_ceg, " +
            "    MAX(nom_empreendimento) as nom_empreendimento, " +
            "    COALESCE(SUM(mda_potencia_outorgada_kw), 0) as potencia " +
            "FROM ralie_usina_csv_import " +
            "WHERE cod_ceg IS NOT NULL AND cod_ceg != '' " +
            "GROUP BY cod_ceg")
    @Modifying
    @Transactional
    void processDataTableLargestGenerators();
}
