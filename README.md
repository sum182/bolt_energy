# Bolt Energy API

API RESTful desenvolvida com Spring Boot para o sistema Bolt Energy.

## 🚀 Tecnologias

- Java 21
- Spring Boot 3.2.0
- Spring Web
- Spring WebFlux (para WebClient reativo)
- Spring Data JPA
- Spring Scheduler
- Lombok
- SpringDoc OpenAPI
- JUnit 5
- Mockito
- SLF4J
- MySQL 8.0.21 (Banco de dados)

## 🐳 Configuração do Banco de Dados

O projeto utiliza o MySQL 8.0.21 em um container Docker para armazenar os metadados dos downloads. Siga os passos abaixo para configurar:

1. **Pré-requisitos**
   - Docker e Docker Compose instalados na sua máquina

2. **Iniciar o container do MySQL**
   ```bash
   docker-compose up -d
   ```
   Isso irá:
   - Baixar a imagem do MySQL 8.0.21 (se ainda não existir)
   - Criar um container chamado `bolt_energy_mysql`
   - Mapear a porta 3306 do container para a porta 3306 da sua máquina
   - Criar um volume chamado `mysql_data` para persistência dos dados

3. **Configurações de acesso**
   - Host: localhost
   - Porta: 3306
   - Banco de dados: bolt_energy_db (será criado automaticamente)
   - Usuário: root
   - Senha: root

4. **Comportamento de inicialização**
   - O banco de dados é criado automaticamente na primeira execução
   - As tabelas são criadas/atualizadas automaticamente pelo Hibernate/JPA
   - O fuso horário está configurado para America/Sao_Paulo
   - O conjunto de caracteres é UTF-8 (utf8mb4)
   - O healthcheck verifica se o MySQL está respondendo
   - Os metadados dos downloads são armazenados na tabela `ralie_metadata`

4. **Verificar se o container está em execução**
   ```bash
   docker ps
   ```

5. **Parar o container (quando necessário)**
   ```bash
   docker-compose down
   ```

## 🏗️ Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/boltenergy/
│   │   ├── config/
│   │   │   ├── AppConfig.java
│   │   │   ├── SwaggerConfig.java
│   │   │   ├── WebClientConfig.java
│   │   │   └── WebClientProperties.java
│   │   │   ├── RalieSchedulingProperties.java
│   │   │   └── SchedulingConfig.java
│   │   │
│   │   ├── controller/
│   │   │   ├── RalieUsinaController.java
│   │   │   └── TestController.java
│   │   │
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── RalieDownloadException.java
│   │   │
│   │   ├── model/
│   │   │   ├── RalieMetadata.java
│   │   │   └── entity/
│   │   │       ├── RalieMetadataEntity.java
│   │   │       ├── RalieUsinaCsvImportEntity.java
│   │   │       └── RalieUsinaEmpresaPotenciaGeradaEntity.java
│   │   │
│   │   ├── service/
│   │   │   ├── AneelRalieService.java
│   │   │   ├── RalieUsinaCsvImportService.java
│   │   │   └── RalieUsinaEmpresaPotenciaGeradaService.java
│   │   │   ├── scheduler/
│   │   │   │   └── RalieDownloadScheduler.java
│   │   │   ├── GoogleService.java
│   │   │   ├── HttpService.java
│   │   │   ├── RalieMetadataService.java
│   │   │   └── RalieMetadataDbService.java
│   │   │   └── repository/
│   │   │       ├── RalieMetadataRepository.java
│   │   │       ├── RalieUsinaCsvImportRepository.java
│   │   │       └── RalieUsinaEmpresaPotenciaGeradaRepository.java
│   │   │
│   │   └── App.java
│   │
│   └── resources/
│       ├── application.yml
│       └── examples/
│           └── ralie-usina-example-simple.csv
│
└── test/
    └── java/com/boltenergy/
        ├── controller/
        │   ├── RalieUsinaControllerTest.java
        │   └── TestControllerTest.java
        ├── integration/
        │   ├── AneelRalieServiceIT.java
        │   └── RalieUsinaCsvImportServiceIT.java
        └── service/
            ├── AneelRalieServiceTest.java
            ├── GoogleServiceTest.java
            ├── HttpServiceTest.java
            ├── RalieMetadataDbServiceTest.java
            ├── RalieUsinaCsvImportServiceTest.java
            ├── RalieUsinaEmpresaPotenciaGeradaServiceTest.java
            └── scheduler/
                └── RalieDownloadSchedulerTest.java

/downloads/
  └── ralie_metadata.json
logs/
  └── application.log
```

## 📋 Pré-requisitos

- Java 21 ou superior
- Maven 3.9+
- Docker e Docker Compose (para o banco de dados MySQL)
- Git (opcional, para controle de versão)

## 🔧 Instalação e Execução

1. Clone o repositório:
   ```bash
   git clone https://github.com/sum182/bolt_energy.git
   cd bolt-energy
   ```

2. Construa o projeto:
   ```bash
   mvn clean install
   ```

3. Execute a aplicação:
   ```bash
   mvn spring-boot:run
   ```


## 🛠️ Comandos Úteis

- Executar a aplicação:
  ```bash
  mvn spring-boot:run
  ```

- Executar testes:
  ```bash
  mvn test
  ```

- Construir o projeto:
  ```bash
  mvn clean package
  ```

- Executar a aplicação a partir do JAR gerado:
  ```bash
  java -jar target/bolt-energy-app-1.0.0-SNAPSHOT.jar
  ```

## 🌐 Endpoints da API

### RALIE Usina Endpoints

- **GET** `/api/ralie-usina/download-csv`
  - Faz o download do arquivo CSV mais recente do Relatório de Acompanhamento da Expansão da Oferta de Geração de Energia Elétrica (RALIE) da ANEEL
  - Processa automaticamente a codificação do arquivo (UTF-8, ISO-8859-1, Windows-1252)
  - Importa os dados para a tabela de importação (`ralie_usina_csv_import`)
  - Processa os dados para a tabela de potência gerada (`ralie_usina_empresa_potencia_gerada`)
  - Retorna o caminho do arquivo CSV baixado

- **POST** `/api/ralie-usina/processa-maiores-geradoras`
  - Processa a tabela de maiores geradoras a partir dos dados importados
  - Agrupa os dados por `cod_ceg` e soma as potências
  - Remove registros existentes antes de processar os novos dados
  - Retorna mensagem de sucesso ou erro

- **GET** `/api/ralie-usina/maiores-geradoras`
  - Retorna a lista das 5 maiores geradoras e suas respectivas potências totais
  - Os dados são obtidos da tabela `ralie_usina_empresa_potencia_gerada`
  - Resposta em formato JSON contendo: `id`, `codCeg`, `nomEmpreendimento` e `potencia`
  - Exemplo de resposta:
    ```json
    [
      {
        "id": 1,
        "codCeg": "A-001",
        "nomEmpreendimento": "Usina Solar Exemplo",
        "potencia": 1500.75
      },
      ...
    ]
    ```





## 🚀 Otimizações Implementadas

O sistema foi otimizado para lidar com download e importação de arquivos grandes de forma eficiente e confiável, com as seguintes características:

1. **Processamento de Dados**
   - Leitura em streaming para arquivos grandes
   - Processamento em lote para melhor desempenho
   - Processamento assíncrono e não-bloqueante
   - Buffer otimizado (1MB) para melhor desempenho

2. **Suporte a Múltiplas Codificações**
   - Detecção automática de codificação (UTF-8, ISO-8859-1, Windows-1252)
   - Correção automática de caracteres especiais
   - Tratamento robusto de diferentes formatos de arquivo

3. **Tratamento de Erros**
   - Timeout de conexão: 5 minutos (configurável)
   - Timeout total de download: 10 minutos
   - Continuação do processamento mesmo com linhas inválidas
   - Logs detalhados para diagnóstico de problemas

4. **Segurança e Confiabilidade**
   - Uso de arquivos temporários durante o download
   - Limpeza automática em caso de falha
   - Verificação de integridade do arquivo
   - Timeout de conexão configurável
   - Suporte a compressão HTTP

5. **Persistência**
   - Armazenamento seguro no banco de dados
   - Atualização em lote para melhor desempenho
   - Rastreamento de metadados de importação
   - Validação de dados durante a importação

### ⚙️ Configurações Personalizáveis

As seguintes configurações podem ser ajustadas no `application.yml`:

```yaml
webclient:
  max-in-memory-size: 50MB
  connect-timeout: 300s  # 5 minutos para conexão
  response-timeout: 300s  # 5 minutos para resposta
  read-timeout: 600s     # 10 minutos para leitura total
  buffer-size: 1MB
```

### ✅ Verificando Modificações no Arquivo CSV

Para verificar se o arquivo CSV foi modificado sem baixar o conteúdo completo, você pode fazer uma requisição HEAD para a URL do arquivo. Isso é útil para verificar se há atualizações disponíveis.

#### Requisição HEAD

```bash
curl --head \
--header 'User-Agent: Mozilla/5.0' \
'https://dadosabertos.aneel.gov.br/dataset/57e4b8b5-a5db-40e6-9901-27ca629d0477/resource/4a615df8-4c25-48fa-bbea-873a36a79518/download/ralie-usina.csv'
```

#### Cabeçalhos Importantes na Resposta

- **ETag**: Identificador único da versão do arquivo
- **Last-Modified**: Data e hora da última modificação
- **Content-Length**: Tamanho do arquivo em bytes

#### Verificando Modificações com Condicionais

Você pode usar os cabeçalhos condicionais para verificar se o arquivo foi modificado desde a última vez que você o baixou:

```bash
curl --head \
--header 'If-None-Match: "seu_etag_aqui"' \
--header 'If-Modified-Since: Wed, 21 Oct 2015 07:28:00 GMT' \
--header 'User-Agent: Mozilla/5.0' \
'https://dadosabertos.aneel.gov.br/dataset/57e4b8b5-a5db-40e6-9901-27ca629d0477/resource/4a615df8-4c25-48fa-bbea-873a36a79518/download/ralie-usina.csv'
```

**Respostas possíveis:**
- **200 OK**: O arquivo foi modificado (novos cabeçalhos serão retornados)
- **304 Not Modified**: O arquivo não foi modificado desde a data/etag fornecida
- **412 Precondition Failed**: As condições fornecidas não foram atendidas

#### Como Usar no Código

A aplicação já implementa essa verificação automaticamente. O serviço `AneelRalieService` verifica se o arquivo foi modificado antes de fazer o download completo, usando os cabeçalhos `ETag` e `Last-Modified`.

## ⏰ Agendamento Automático

O sistema possui um agendador automático que pode ser configurado para baixar periodicamente o arquivo RALIE da ANEEL.

### Configuração

As configurações de agendamento podem ser ajustadas no arquivo `application.yml`:

```yaml
ralie:
  schedule:
    # Expressão cron para agendamento (padrão: a cada hora)
    # Exemplos:
    # 0 * * * * *   - A cada minuto
    # 0 */5 * * * * - A cada 5 minutos
    # 0 0 * * * *   - A cada hora (no minuto 0)
    # 0 0 */2 * * * - A cada 2 horas
    cron: 0 0 * * * *
    
    # Habilita/desabilita o agendamento automático
    enabled: true
    
    # Nome do job para logs
    job-name: "RALIE Download Job"
```

### Como Funciona

- O agendador verifica periodicamente se há uma nova versão do arquivo RALIE disponível
- Se uma nova versão for encontrada, o download é realizado automaticamente
- O histórico de downloads é mantido no arquivo de metadados
- O agendamento pode ser habilitado/desabilitado conforme necessário


### 📄 Logs

O endpoint de download inclui logs detalhados que ajudam a monitorar o desempenho:

```
- Iniciando download do arquivo: [URL]
- Download concluído em X.XXs - Tamanho: Y.YYMB - Velocidade média: Z.ZZMB/s
- Erros detalhados em caso de falha
```

## 📊 Documentação e Monitoramento

### Documentação da API
A documentação interativa da API está disponível através do Swagger UI:
- [Swagger UI](http://localhost:8182/swagger-ui.html)
- [OpenAPI JSON](http://localhost:8182/v3/api-docs)

### Spring Boot Actuator
Endpoints de monitoramento e gerenciamento:
- [Health Check](http://localhost:8182/actuator/health) - Status de saúde da aplicação
- [Metrics](http://localhost:8182/actuator/metrics) - Métricas da aplicação
- [Environment](http://localhost:8182/actuator/env) - Variáveis de ambiente
- [Mappings](http://localhost:8182/actuator/mappings) - Mapeamentos de endpoints
- [Beans](http://localhost:8182/actuator/beans) - Beans do Spring
- [Info](http://localhost:8182/actuator/info) - Informações da aplicação

## 🧪 Testes

Para executar os testes:
```bash
mvn test
```

## 📡 Collections do Postman

O projeto inclui collections do Postman para facilitar os testes da API. As collections estão localizadas no diretório `postman/`:

- `aneel.postman_collection.json`: Collection para os endpoints relacionados à integração com a ANEEL
- `bolt_energi_api.postman_collection.json`: Collection principal com todos os endpoints da API

### Como importar as collections

1. Abra o Postman
2. Clique em "Import" no canto superior esquerdo
3. Selecione os arquivos `.json` do diretório `postman/`
4. As collections estarão disponíveis na aba "Collections"

## 🤖 Inteligência Artificial - Windsurf

Este projeto utiliza o Windsurf, uma ferramenta de IA integrada ao IntelliJ IDEA, para auxiliar no desenvolvimento.

### Configuração
- Instale a extensão Windsurf no IntelliJ
- Faça login com sua conta
- O assistente estará disponível na IDE

### Regras do Projeto
O arquivo `.windsurf/rules.json` define:
- Padrões de nomenclatura (PascalCase, camelCase)
- Convenções de código
- Diretrizes de documentação
- Estrutura de pacotes



