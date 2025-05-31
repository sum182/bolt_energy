# Bolt Energy API

API RESTful desenvolvida com Spring Boot para o sistema Bolt Energy.

## 🚀 Tecnologias

- Java 21
- Spring Boot 3.2.0
- Spring Web
- Spring WebFlux (para WebClient reativo)
- Lombok
- SpringDoc OpenAPI (Documentação)
- JUnit 5 (Testes)
- Mockito (Mocks para testes)
- SLF4J (Logging)
- Reactor (Programação reativa)

## 🏗️ Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/boltenergy/
│   │   ├── config/                  # Configurações da aplicação
│   │   │   ├── AppConfig.java        # Configurações gerais da aplicação
│   │   │   ├── SwaggerConfig.java    # Configuração do Swagger/OpenAPI
│   │   │   ├── WebClientConfig.java  # Configuração central do WebClient
│   │   │   └── WebClientProperties.java # Propriedades do WebClient
│   │   │
│   │   ├── controller/            # Controladores da API
│   │   │   ├── RalieUsinaController.java  # Endpoints para dados RALIE
│   │   │   └── TestController.java   # Endpoints de teste
│   │   │
│   │   ├── exception/             # Tratamento de exceções
│   │   │   ├── GlobalExceptionHandler.java # Tratamento global de exceções
│   │   │   └── RalieDownloadException.java # Exceção específica para falhas no download
│   │   │
│   │   ├── model/                 # Modelos de dados
│   │   │   └── RalieMetadata.java    # Metadados para controle de downloads
│   │   │
│   │   ├── service/               # Lógica de negócios
│   │   │   ├── AneelRalieService.java   # Serviço para integração com dados da ANEEL
│   │   │   ├── GoogleService.java    # Serviço para integração com Google
│   │   │   ├── HttpService.java      # Serviço genérico HTTP
│   │   │   └── RalieMetadataService.java # Gerenciamento de metadados
│   │   │
│   │   └── App.java          # Classe principal da aplicação
│   │
│   └── resources/                 # Recursos da aplicação
│       ├── application.yml           # Configurações da aplicação
│       └── examples/                 # Exemplos de arquivos
│           └── ralie-usina-example-simple.csv  # Exemplo de arquivo RALIE
│
└── test/                           # Testes unitários e de integração
    └── java/com/boltenergy/
        ├── controller/                # Testes dos controladores
        │   ├── RalieUsinaControllerTest.java
        │   └── TestControllerTest.java
        └── service/                   # Testes dos serviços
            └── AneelRalieServiceTest.java

# Diretórios gerados em tempo de execução
/downloads/                     # Arquivos baixados (criado em tempo de execução)
  └── ralie_metadata.json       # Metadados dos downloads (criado em tempo de execução)
logs/                           # Arquivos de log (criado em tempo de execução)
  └── application.log           # Logs da aplicação
```

## 📋 Pré-requisitos

- Java 21 ou superior
- Maven 3.9+
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

4. Acesse a aplicação em: [http://localhost:8182/api/hello](http://localhost:8182/api/hello)

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
  - Retorna o arquivo CSV para download


### Test Endpoints
- **GET** `/api/test/hello`
  - Retorna uma mensagem de boas-vindas de teste
  
  Exemplo de resposta:
  ```
  Bem-vindo ao endpoint de teste da Bolt Energy!
  ```

- **GET** `/api/test/google`
  - Retorna o HTML da página inicial do Google (chamada síncrona)

  
  Exemplo de resposta:
  ```html
  <!doctype html>...</html>
  ```

- **GET** `/api/test/google/async`
  - Retorna o HTML da página inicial do Google (chamada assíncrona)
  - Retorna um `Mono<String>`
  
  Exemplo de resposta:
  ```html
  <!doctype html>...</html>
  ```
  Bem-vindo à API da Bolt Energy!
  ```

## 🚀 Otimização para Download de Arquivos

Foram implementadas algumas otimizações para lidar com o download de arquivos grandes de forma eficiente e confiável:

### 🛠️ Otimizações Implementadas

1. **Streaming de Dados**
   - Processamento do arquivo em blocos para evitar consumo excessivo de memória
   - Processamento assíncrono e não-bloqueante
   - Buffer otimizado para melhor desempenho

2. **Tratamento de Erros**
   - Timeout de conexão: 5 minutos (configurável)
   - Timeout total de download: 10 minutos
   - Tratamento específico para diferentes tipos de falhas
   - Logs detalhados para diagnóstico de problemas

3. **Segurança e Confiabilidade**
   - Uso de arquivos temporários durante o download
   - Limpeza automática em caso de falha
   - Verificação de integridade do arquivo

4. **Otimizações de Desempenho**
   - Buffer de rede otimizado (1MB)
   - Timeout de conexão configurável
   - Suporte a compressão HTTP

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

### 📊 Monitoramento

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



