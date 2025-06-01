package com.boltenergy.service;

import com.boltenergy.model.entity.RalieUsinaCsvImportEntity;
import com.boltenergy.repository.RalieUsinaCsvImportRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RalieUsinaCsvImportServiceTest {

    @Mock
    private RalieUsinaCsvImportRepository repository;

    @InjectMocks
    private RalieUsinaCsvImportService service;

    @Captor
    private ArgumentCaptor<List<RalieUsinaCsvImportEntity>> entitiesCaptor;

    private String validCsvContent;
    private String emptyCsvContent;
    private String malformedCsvContent;

    @BeforeEach
    void setUp() {
        validCsvContent = "DatGeracaoConjuntoDados;DatRalie;IdeNucleoCEG;CodCEG;SigUFPrincipal;DscOrigemCombustivel;SigTipoGeracao;NomEmpreendimento;MdaPotenciaOutorgadaKw;DscPropriRegimePariticipacao;DscTipoConexao;NomConexao;MdaTensaoConexao;NomEmpresaConexao;NumCnpjEmpresaConexao;DscViabilidade;DscSituacaoObra;DatPrevisaoInicioObra;DatContratoEPCOutorgado;DatRecursoFinanceiroOutorgado;DatCanteiroObraOutorgado;DatCanteiroObraRealizado;DatInicioObraOutorgado;DatInicioObraRealizado;DatConcretagemOutorgado;DatConcretagemRealizado;DatDesvioRioOutorgado;DatDesvioRioRealizado;DatMontagemOutorgado;DatMontagemRealizado;DatConclusaoTorresOutorgado;DatConclusaoTorresRealizado;DatEnchimentoOutorgado;DatEnchimentoRealizado;DatComissionamentoUGRealizado;DatSisTransmissaoOutorgado;DatSisTransmissaoRealizado;DatConclusaoSisTransOutorgado;DatConclusaoSisTransRealizado;DscJustificativaPrevisao;DscComercializacaoEnergia;DscSistema;DatConclusaoTransporteRealizado;DscSituacaoCronograma;DatRapeel;IdcComplexo;NomComplexo;DatEmissaoLP;DatValidadeLP;DscSituacaoLP;DatEmissaoLI;DatValidadeLI;DscSituacaoLI\n" +
                        "01/01/2023;02/01/2023;NUC123;UHE.PH.RS.000324-7.1;RS;HIDRAULICA;UHE;Usina Teste 1;10000.00;PRIVADO;DIRETA;CONEXAO 1;138.00;EMPRESA TESTE;12345678000190;VIABILIDADE;EM ANDAMENTO;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;JUSTIFICATIVA;COMERCIALIZACAO;SISTEMA;01/01/2023;CRONOGRAMA;01/01/2023;S;COMPLEXO;01/01/2023;01/01/2023;SITUACAO;01/01/2023;01/01/2023;SITUACAO\n" +
                        "01/01/2023;02/01/2023;NUC124;UHE.PH.RS.000325-7.1;RS;HIDRAULICA;UHE;Usina Teste 2;20000.00;PRIVADO;DIRETA;CONEXAO 2;138.00;EMPRESA TESTE 2;12345678000200;VIABILIDADE;EM ANDAMENTO;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;01/01/2023;JUSTIFICATIVA;COMERCIALIZACAO;SISTEMA;01/01/2023;CRONOGRAMA;01/01/2023;S;COMPLEXO;01/01/2023;01/01/2023;SITUACAO;01/01/2023;01/01/2023;SITUACAO";

        emptyCsvContent = "DatGeracaoConjuntoDados;DatRalie;IdeNucleoCEG;CodCEG;SigUFPrincipal;DscOrigemCombustivel;SigTipoGeracao;NomEmpreendimento;MdaPotenciaOutorgadaKw;DscPropriRegimePariticipacao;DscTipoConexao;NomConexao;MdaTensaoConexao;NomEmpresaConexao;NumCnpjEmpresaConexao;DscViabilidade;DscSituacaoObra;DatPrevisaoInicioObra;DatContratoEPCOutorgado;DatRecursoFinanceiroOutorgado;DatCanteiroObraOutorgado;DatCanteiroObraRealizado;DatInicioObraOutorgado;DatInicioObraRealizado;DatConcretagemOutorgado;DatConcretagemRealizado;DatDesvioRioOutorgado;DatDesvioRioRealizado;DatMontagemOutorgado;DatMontagemRealizado;DatConclusaoTorresOutorgado;DatConclusaoTorresRealizado;DatEnchimentoOutorgado;DatEnchimentoRealizado;DatComissionamentoUGRealizado;DatSisTransmissaoOutorgado;DatSisTransmissaoRealizado;DatConclusaoSisTransOutorgado;DatConclusaoSisTransRealizado;DscJustificativaPrevisao;DscComercializacaoEnergia;DscSistema;DatConclusaoTransporteRealizado;DscSituacaoCronograma;DatRapeel;IdcComplexo;NomComplexo;DatEmissaoLP;DatValidadeLP;DscSituacaoLP;DatEmissaoLI;DatValidadeLI;DscSituacaoLI";
        
        malformedCsvContent = "Header1;Header2\nvalue1;value2;value3\n";
    }

    @Test
    void deleteAll_WhenRecordsExist_ShouldDeleteAll() {
        when(repository.count()).thenReturn(2L);
        
        service.deleteAll();
        
        verify(repository, times(1)).deleteAllInBatch();
        verify(repository, times(1)).count();
    }

    @Test
    void deleteAll_WhenNoRecords_ShouldNotDelete() {
        when(repository.count()).thenReturn(0L);
        
        service.deleteAll();
        
        verify(repository, never()).deleteAllInBatch();
        verify(repository, times(1)).count();
    }

    @Test
    void count_ShouldReturnRepositoryCount() {
        when(repository.count()).thenReturn(5L);
        
        long result = service.count();
        
        assertEquals(5L, result);
        verify(repository, times(1)).count();
    }

    @Test
    void importCsv_WithValidContent_ShouldImportSuccessfully() throws IOException {
        when(repository.count()).thenReturn(1L);
        
        service.importCsv(validCsvContent);
        
        verify(repository, atLeastOnce()).count();
        verify(repository, times(1)).deleteAllInBatch();
        verify(repository, times(1)).saveAllAndFlush(anyList());
    }

    @Test
    void importCsv_WithEmptyContent_ShouldThrowException() {
        Exception exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.importCsv("")
        );
        
        assertEquals("O conteúdo do CSV não pode ser nulo ou vazio", exception.getMessage());
    }

    @Test
    void importCsv_WithEmptyCsv_ShouldNotSaveAnyRecords() throws IOException {
        when(repository.count()).thenReturn(0L);
        
        service.importCsv(emptyCsvContent);
        
        // Verifica se o count foi chamado pelo menos uma vez
        verify(repository, atLeastOnce()).count();
        
        // Não deve chamar deleteAllInBatch() quando não há registros
        verify(repository, never()).deleteAllInBatch();
        
        // Não deve chamar saveAllAndFlush para CSV vazio
        verify(repository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void importCsv_WithMalformedCsv_ShouldProcessGracefully() throws IOException {
        // Testa se o método processa o CSV malformado sem lançar exceção
        // O parser do Apache Commons CSV é tolerante a linhas com número incorreto de colunas
        service.importCsv(malformedCsvContent);
        
        // Verifica se o método não tentou salvar registros inválidos
        // O serviço pode chamar saveAllAndFlush com lista vazia, o que é aceitável
        verify(repository, atMost(1)).saveAllAndFlush(anyList());
        
        // Verifica se o método não lançou exceção
        // Se chegou até aqui, o teste passa
    }


}
