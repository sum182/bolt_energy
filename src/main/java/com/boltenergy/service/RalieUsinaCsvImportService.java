package com.boltenergy.service;

import com.boltenergy.model.entity.RalieUsinaCsvImportEntity;
import com.boltenergy.repository.RalieUsinaCsvImportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class RalieUsinaCsvImportService {

    private final RalieUsinaCsvImportRepository repository;

    private String fixEncoding(String text) {
        if (text == null) return null;
        
        // Se o texto já está em UTF-8 válido, retorna sem alterações
        if (isValidUtf8(text)) {
            return text;
        }
        
        // Tenta converter de ISO-8859-1 para UTF-8
        try {
            byte[] bytes = text.getBytes("ISO-8859-1");
            String converted = new String(bytes, "UTF-8");
            if (isValidUtf8(converted)) {
                return converted;
            }
        } catch (Exception e) {
            log.debug("Não foi possível converter de ISO-8859-1: {}", e.getMessage());
        }
        
        // Tenta converter de Windows-1252 para UTF-8
        try {
            byte[] bytes = text.getBytes("Windows-1252");
            String converted = new String(bytes, "UTF-8");
            if (isValidUtf8(converted)) {
                return converted;
            }
        } catch (Exception e) {
            log.debug("Não foi possível converter de Windows-1252: {}", e.getMessage());
        }
        
        // Se nada funcionou, retorna o texto original
        log.warn("Não foi possível determinar a codificação correta para o texto: {}", text);
        return text;
    }
    
    private boolean isValidUtf8(String text) {
        try {
            // Tenta codificar para UTF-8 e decodificar de volta
            // Se não lançar exceção, é UTF-8 válido
            byte[] bytes = text.getBytes("UTF-8");
            String decoded = new String(bytes, "UTF-8");
            return text.equals(decoded);
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public void deleteAll() {
        log.info("Removendo todos os registros existentes da tabela de importação");
        if (repository.count() > 0) {
            repository.deleteAllInBatch();
            log.info("Todos os registros foram removidos com sucesso");
        } else {
            log.info("Nenhum registro encontrado para remoção");
        }
    }
    
    @Transactional
    public void importCsv(String csvContent) throws IOException {
        deleteAll();
        String fixedContent = fixEncoding(csvContent);
        log.info("Iniciando importação do CSV para a tabela de importação");
        
        try (CSVParser parser = new CSVParser(
                new StringReader(fixedContent),
                CSVFormat.DEFAULT.builder()
                    .setDelimiter(';')
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build())) {
            
            int count = 0;
            for (CSVRecord record : parser) {
                RalieUsinaCsvImportEntity entity = new RalieUsinaCsvImportEntity();
                mapRecordToEntity(record, entity);
                repository.save(entity);
                count++;
                
                if (count % 1000 == 0) {
                    log.info("Registros processados: {}", count);
                }
            }
            
            log.info("Importação concluída. Total de registros importados: {}", count);
        }
    }
    
    private void mapRecordToEntity(CSVRecord record, RalieUsinaCsvImportEntity entity) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        setIfExists(record, "DatGeracaoConjuntoDados", v -> entity.setDatGeracaoConjuntoDados(parseDate(v, dateFormatter)));
        setIfExists(record, "DatRalie", v -> entity.setDatRalie(parseDate(v, dateFormatter)));
        setIfExists(record, "IdeNucleoCEG", entity::setIdeNucleoCeg);
        setIfExists(record, "CodCEG", entity::setCodCeg);
        setIfExists(record, "SigUFPrincipal", entity::setSigUfPrincipal);
        setIfExists(record, "DscOrigemCombustivel", entity::setDscOrigemCombustivel);
        setIfExists(record, "SigTipoGeracao", entity::setSigTipoGeracao);
        setIfExists(record, "NomEmpreendimento", entity::setNomEmpreendimento);
        setIfExists(record, "MdaPotenciaOutorgadaKw", v -> entity.setMdaPotenciaOutorgadaKw(parseDouble(v)));
        setIfExists(record, "DscPropriRegimePariticipacao", entity::setDscPropriRegimePariticipacao);
        setIfExists(record, "DscTipoConexao", entity::setDscTipoConexao);
        setIfExists(record, "NomConexao", entity::setNomConexao);
        setIfExists(record, "MdaTensaoConexao", entity::setMdaTensaoConexao);
        setIfExists(record, "NomEmpresaConexao", entity::setNomEmpresaConexao);
        setIfExists(record, "NumCnpjEmpresaConexao", entity::setNumCnpjEmpresaConexao);
        setIfExists(record, "DscViabilidade", entity::setDscViabilidade);
        setIfExists(record, "DscSituacaoObra", entity::setDscSituacaoObra);
        
        // Mapear datas
        setIfExists(record, "DatPrevisaoInicioObra", v -> entity.setDatPrevisaoInicioObra(parseDate(v, dateFormatter)));
        setIfExists(record, "DatContratoEPCOutorgado", v -> entity.setDatContratoEpcOutorgado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatRecursoFinanceiroOutorgado", v -> entity.setDatRecursoFinanceiroOutorgado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatCanteiroObraOutorgado", v -> entity.setDatCanteiroObraOutorgado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatCanteiroObraRealizado", v -> entity.setDatCanteiroObraRealizado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatInicioObraOutorgado", v -> entity.setDatInicioObraOutorgado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatInicioObraRealizado", v -> entity.setDatInicioObraRealizado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatConcretagemOutorgado", v -> entity.setDatConcretagemOutorgado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatConcretagemRealizado", v -> entity.setDatConcretagemRealizado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatDesvioRioOutorgado", v -> entity.setDatDesvioRioOutorgado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatDesvioRioRealizado", v -> entity.setDatDesvioRioRealizado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatMontagemOutorgado", v -> entity.setDatMontagemOutorgado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatMontagemRealizado", v -> entity.setDatMontagemRealizado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatConclusaoTorresOutorgado", v -> entity.setDatConclusaoTorresOutorgado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatConclusaoTorresRealizado", v -> entity.setDatConclusaoTorresRealizado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatEnchimentoOutorgado", v -> entity.setDatEnchimentoOutorgado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatEnchimentoRealizado", v -> entity.setDatEnchimentoRealizado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatComissionamentoUGRealizado", v -> entity.setDatComissionamentoUgRealizado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatSisTransmissaoOutorgado", v -> entity.setDatSisTransmissaoOutorgado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatSisTransmissaoRealizado", v -> entity.setDatSisTransmissaoRealizado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatConclusaoSisTransOutorgado", v -> entity.setDatConclusaoSisTransOutorgado(parseDate(v, dateFormatter)));
        setIfExists(record, "DatConclusaoSisTransRealizado", v -> entity.setDatConclusaoSisTransRealizado(parseDate(v, dateFormatter)));
        
        setIfExists(record, "DscJustificativaPrevisao", entity::setDscJustificativaPrevisao);
        setIfExists(record, "DscComercializacaoEnergia", entity::setDscComercializacaoEnergia);
        setIfExists(record, "DscSistema", entity::setDscSistema);
        setIfExists(record, "DatConclusaoTransporteRealizado", v -> entity.setDatConclusaoTransporteRealizado(parseDate(v, dateFormatter)));
        setIfExists(record, "DscSituacaoCronograma", entity::setDscSituacaoCronograma);
        setIfExists(record, "DatRapeel", v -> entity.setDatRapeel(parseDate(v, dateFormatter)));
        setIfExists(record, "IdcComplexo", entity::setIdcComplexo);
        setIfExists(record, "NomComplexo", entity::setNomComplexo);
        setIfExists(record, "DatEmissaoLP", v -> entity.setDatEmissaoLp(parseDate(v, dateFormatter)));
        setIfExists(record, "DatValidadeLP", v -> entity.setDatValidadeLp(parseDate(v, dateFormatter)));
        setIfExists(record, "DscSituacaoLP", entity::setDscSituacaoLp);
        setIfExists(record, "DatEmissaoLI", v -> entity.setDatEmissaoLi(parseDate(v, dateFormatter)));
        setIfExists(record, "DatValidadeLI", v -> entity.setDatValidadeLi(parseDate(v, dateFormatter)));
        setIfExists(record, "DscSituacaoLI", entity::setDscSituacaoLi);
        setIfExists(record, "DatSolicitacaoLO", v -> entity.setDatSolicitacaoLo(parseDate(v, dateFormatter)));
        setIfExists(record, "DatPrevistaEmissaoLO", v -> entity.setDatPrevistaEmissaoLo(parseDate(v, dateFormatter)));
        setIfExists(record, "DatPrevMaxEmissaoLO", v -> entity.setDatPrevMaxEmissaoLo(parseDate(v, dateFormatter)));
        setIfExists(record, "DatEmissaoLO", v -> entity.setDatEmissaoLo(parseDate(v, dateFormatter)));
        setIfExists(record, "DatValidadeLO", v -> entity.setDatValidadeLo(parseDate(v, dateFormatter)));
        setIfExists(record, "DscSituacaoLO", entity::setDscSituacaoLo);
        setIfExists(record, "NomSituacaoParAcesso", entity::setNomSituacaoParAcesso);
        setIfExists(record, "DatSolicitacaoParAcesso", v -> entity.setDatSolicitacaoParAcesso(parseDate(v, dateFormatter)));
        setIfExists(record, "DatEmissaoParAcesso", v -> entity.setDatEmissaoParAcesso(parseDate(v, dateFormatter)));
        setIfExists(record, "DscSitCCD", entity::setDscSitCcd);
        setIfExists(record, "DatValidadeCCD", v -> entity.setDatValidadeCcd(parseDate(v, dateFormatter)));
        setIfExists(record, "DatAssinaturaCCD", v -> entity.setDatAssinaturaCcd(parseDate(v, dateFormatter)));
        setIfExists(record, "DscSitCCT", entity::setDscSitCct);
        setIfExists(record, "DatValidadeCCT", v -> entity.setDatValidadeCct(parseDate(v, dateFormatter)));
        setIfExists(record, "DatAssinaturaCCT", v -> entity.setDatAssinaturaCct(parseDate(v, dateFormatter)));
        setIfExists(record, "DscSituacaoCUSD", entity::setDscSituacaoCusd);
        setIfExists(record, "DatValidadeCUSD", v -> entity.setDatValidadeCusd(parseDate(v, dateFormatter)));
        setIfExists(record, "DatAssinaturaCUSD", v -> entity.setDatAssinaturaCusd(parseDate(v, dateFormatter)));
        setIfExists(record, "DscSitCUST", entity::setDscSitCust);
        setIfExists(record, "DatValidadeCUST", v -> entity.setDatValidadeCust(parseDate(v, dateFormatter)));
        setIfExists(record, "DatAssinaturaCUST", v -> entity.setDatAssinaturaCust(parseDate(v, dateFormatter)));
        setIfExists(record, "DscAtoOutorga", entity::setDscAtoOutorga);
        setIfExists(record, "DscNumeroAto", entity::setDscNumeroAto);
        setIfExists(record, "NomOrgaoOutorgante", entity::setNomOrgaoOutorgante);
        setIfExists(record, "DscTipoOutorga", entity::setDscTipoOutorga);
        setIfExists(record, "DatEmissaoAto", v -> entity.setDatEmissaoAto(parseDate(v, dateFormatter)));
    }
    
    private void setIfExists(CSVRecord record, String column, java.util.function.Consumer<String> setter) {
        try {
            if (record.isSet(column)) {
                String value = record.get(column);
                if (value != null && !value.trim().isEmpty()) {
                    // Corrige a codificação do valor antes de definir
                    String fixedValue = fixEncoding(value);
                    setter.accept(fixedValue);
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao processar coluna '{}': {}", column, e.getMessage());
        }
    }
    
    private LocalDate parseDate(String dateStr, DateTimeFormatter formatter) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            return null;
        }
    }
    
    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.replace(",", "."));
        } catch (Exception e) {
            return null;
        }
    }
}
