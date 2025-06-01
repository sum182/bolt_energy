package com.boltenergy.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RalieUsinaEmpresaPotenciaGeradaDTO {
    
    private Long id;
    private String codCeg;
    private String nomEmpreendimento;
    private BigDecimal potencia;
    
    public static RalieUsinaEmpresaPotenciaGeradaDTO fromEntity(com.boltenergy.model.entity.RalieUsinaEmpresaPotenciaGeradaEntity entity) {
        return RalieUsinaEmpresaPotenciaGeradaDTO.builder()
            .id(entity.getId())
            .codCeg(entity.getCodCeg())
            .nomEmpreendimento(entity.getNomEmpreendimento())
            .potencia(entity.getPotencia())
            .build();
    }
    
    public com.boltenergy.model.entity.RalieUsinaEmpresaPotenciaGeradaEntity toEntity() {
        return com.boltenergy.model.entity.RalieUsinaEmpresaPotenciaGeradaEntity.builder()
            .id(this.id)
            .codCeg(this.codCeg)
            .nomEmpreendimento(this.nomEmpreendimento)
            .potencia(this.potencia)
            .build();
    }
}
