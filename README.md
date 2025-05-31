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
│   │   ├── config/           # Configurações da aplicação
│   │   │   └── WebClientConfig.java  # Configuração central do WebClient
│   │   │   └── SwaggerConfig.java    # Configuração do Swagger/OpenAPI
│   │   ├── controller/       # Controladores da API
│   │   │   └── TestController.java   # Endpoints de teste
│   │   ├── exception/        # Tratamento de exceções
│   │   ├── service/          # Lógica de negócios
│   │   │   ├── GoogleService.java    # Serviço para integração com Google
│   │   │   └── HttpService.java      # Serviço genérico HTTP
│   │   └── App.java          # Classe principal
│   └── resources/
│       ├── application.yml    # Configurações da aplicação
│       └── logback-spring.xml # Configuração de logs
└── test/                     # Testes unitários e de integração
    └── java/com/boltenergy/
        └── controller/
            └── TestControllerTest.java
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



Desenvolvido com ❤️ pela Equipe Bolt Energy
