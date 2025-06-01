package com.boltenergy.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ralie_usina_csv_import")
public class RalieUsinaCsvImportEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "data_importacao")
    private LocalDateTime dataImportacao;
    
    @Column(name = "dat_geracao_conjunto_dados")
    private LocalDate datGeracaoConjuntoDados;
    
    @Column(name = "dat_ralie")
    private LocalDate datRalie;
    
    @Column(name = "ide_nucleo_ceg")
    private String ideNucleoCeg;
    
    @Column(name = "cod_ceg")
    private String codCeg;
    
    @Column(name = "sig_uf_principal")
    private String sigUfPrincipal;
    
    @Column(name = "dsc_origem_combustivel")
    private String dscOrigemCombustivel;
    
    @Column(name = "sig_tipo_geracao")
    private String sigTipoGeracao;
    
    @Column(name = "nom_empreendimento")
    private String nomEmpreendimento;
    
    @Column(name = "mda_potencia_outorgada_kw")
    private Double mdaPotenciaOutorgadaKw;
    
    @Column(name = "dsc_propri_regime_pariticipacao")
    private String dscPropriRegimePariticipacao;
    
    @Column(name = "dsc_tipo_conexao")
    private String dscTipoConexao;
    
    @Column(name = "nom_conexao")
    private String nomConexao;
    
    @Column(name = "mda_tensao_conexao")
    private String mdaTensaoConexao;
    
    @Column(name = "nom_empresa_conexao")
    private String nomEmpresaConexao;
    
    @Column(name = "num_cnpj_empresa_conexao")
    private String numCnpjEmpresaConexao;
    
    @Column(name = "dsc_viabilidade")
    private String dscViabilidade;
    
    @Column(name = "dsc_situacao_obra")
    private String dscSituacaoObra;
    
    @Column(name = "dat_previsao_inicio_obra")
    private LocalDate datPrevisaoInicioObra;
    
    @Column(name = "dat_contrato_epc_outorgado")
    private LocalDate datContratoEpcOutorgado;
    
    @Column(name = "dat_recurso_financeiro_outorgado")
    private LocalDate datRecursoFinanceiroOutorgado;
    
    @Column(name = "dat_canteiro_obra_outorgado")
    private LocalDate datCanteiroObraOutorgado;
    
    @Column(name = "dat_canteiro_obra_realizado")
    private LocalDate datCanteiroObraRealizado;
    
    @Column(name = "dat_inicio_obra_outorgado")
    private LocalDate datInicioObraOutorgado;
    
    @Column(name = "dat_inicio_obra_realizado")
    private LocalDate datInicioObraRealizado;
    
    @Column(name = "dat_concretagem_outorgado")
    private LocalDate datConcretagemOutorgado;
    
    @Column(name = "dat_concretagem_realizado")
    private LocalDate datConcretagemRealizado;
    
    @Column(name = "dat_desvio_rio_outorgado")
    private LocalDate datDesvioRioOutorgado;
    
    @Column(name = "dat_desvio_rio_realizado")
    private LocalDate datDesvioRioRealizado;
    
    @Column(name = "dat_montagem_outorgado")
    private LocalDate datMontagemOutorgado;
    
    @Column(name = "dat_montagem_realizado")
    private LocalDate datMontagemRealizado;
    
    @Column(name = "dat_conclusao_torres_outorgado")
    private LocalDate datConclusaoTorresOutorgado;
    
    @Column(name = "dat_conclusao_torres_realizado")
    private LocalDate datConclusaoTorresRealizado;
    
    @Column(name = "dat_enchimento_outorgado")
    private LocalDate datEnchimentoOutorgado;
    
    @Column(name = "dat_enchimento_realizado")
    private LocalDate datEnchimentoRealizado;
    
    @Column(name = "dat_comissionamento_ug_realizado")
    private LocalDate datComissionamentoUgRealizado;
    
    @Column(name = "dat_sis_transmissao_outorgado")
    private LocalDate datSisTransmissaoOutorgado;
    
    @Column(name = "dat_sis_transmissao_realizado")
    private LocalDate datSisTransmissaoRealizado;
    
    @Column(name = "dat_conclusao_sis_trans_outorgado")
    private LocalDate datConclusaoSisTransOutorgado;
    
    @Column(name = "dat_conclusao_sis_trans_realizado")
    private LocalDate datConclusaoSisTransRealizado;
    
    @Column(name = "dsc_justificativa_previsao")
    private String dscJustificativaPrevisao;
    
    @Column(name = "dsc_comercializacao_energia")
    private String dscComercializacaoEnergia;
    
    @Column(name = "dsc_sistema")
    private String dscSistema;
    
    @Column(name = "dat_conclusao_transporte_realizado")
    private LocalDate datConclusaoTransporteRealizado;
    
    @Column(name = "dsc_situacao_cronograma")
    private String dscSituacaoCronograma;
    
    @Column(name = "dat_rapeel")
    private LocalDate datRapeel;
    
    @Column(name = "idc_complexo")
    private String idcComplexo;
    
    @Column(name = "nom_complexo")
    private String nomComplexo;
    
    @Column(name = "dat_emissao_lp")
    private LocalDate datEmissaoLp;
    
    @Column(name = "dat_validade_lp")
    private LocalDate datValidadeLp;
    
    @Column(name = "dsc_situacao_lp")
    private String dscSituacaoLp;
    
    @Column(name = "dat_emissao_li")
    private LocalDate datEmissaoLi;
    
    @Column(name = "dat_validade_li")
    private LocalDate datValidadeLi;
    
    @Column(name = "dsc_situacao_li")
    private String dscSituacaoLi;
    
    @Column(name = "dat_solicitacao_lo")
    private LocalDate datSolicitacaoLo;
    
    @Column(name = "dat_prevista_emissao_lo")
    private LocalDate datPrevistaEmissaoLo;
    
    @Column(name = "dat_prev_max_emissao_lo")
    private LocalDate datPrevMaxEmissaoLo;
    
    @Column(name = "dat_emissao_lo")
    private LocalDate datEmissaoLo;
    
    @Column(name = "dat_validade_lo")
    private LocalDate datValidadeLo;
    
    @Column(name = "dsc_situacao_lo")
    private String dscSituacaoLo;
    
    @Column(name = "nom_situacao_par_acesso")
    private String nomSituacaoParAcesso;
    
    @Column(name = "dat_solicitacao_par_acesso")
    private LocalDate datSolicitacaoParAcesso;
    
    @Column(name = "dat_emissao_par_acesso")
    private LocalDate datEmissaoParAcesso;
    
    @Column(name = "dsc_sit_ccd")
    private String dscSitCcd;
    
    @Column(name = "dat_validade_ccd")
    private LocalDate datValidadeCcd;
    
    @Column(name = "dat_assinatura_ccd")
    private LocalDate datAssinaturaCcd;
    
    @Column(name = "dsc_sit_cct")
    private String dscSitCct;
    
    @Column(name = "dat_validade_cct")
    private LocalDate datValidadeCct;
    
    @Column(name = "dat_assinatura_cct")
    private LocalDate datAssinaturaCct;
    
    @Column(name = "dsc_situacao_cusd")
    private String dscSituacaoCusd;
    
    @Column(name = "dat_validade_cusd")
    private LocalDate datValidadeCusd;
    
    @Column(name = "dat_assinatura_cusd")
    private LocalDate datAssinaturaCusd;
    
    @Column(name = "dsc_sit_cust")
    private String dscSitCust;
    
    @Column(name = "dat_validade_cust")
    private LocalDate datValidadeCust;
    
    @Column(name = "dat_assinatura_cust")
    private LocalDate datAssinaturaCust;
    
    @Column(name = "dsc_ato_outorga")
    private String dscAtoOutorga;
    
    @Column(name = "dsc_numero_ato")
    private String dscNumeroAto;
    
    @Column(name = "nom_orgao_outorgante")
    private String nomOrgaoOutorgante;
    
    @Column(name = "dsc_tipo_outorga")
    private String dscTipoOutorga;
    
    @Column(name = "dat_emissao_ato")
    private LocalDate datEmissaoAto;
    
    @PrePersist
    protected void onCreate() {
        this.dataImportacao = LocalDateTime.now();
    }
}
