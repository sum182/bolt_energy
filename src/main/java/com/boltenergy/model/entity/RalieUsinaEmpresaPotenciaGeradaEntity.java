package com.boltenergy.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ralie_usina_empresa_potencia_gerada")
public class RalieUsinaEmpresaPotenciaGeradaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cod_ceg", nullable = false, length = 50)
    private String codCeg;
    
    @Column(name = "nom_empreendimento", nullable = false, length = 255)
    private String nomEmpreendimento;
    
    @Column(name = "potencia", nullable = false)
    private Double potencia;
}
