package com.boltenergy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Swagger/OpenAPI para documentação da API.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configura as informações da API para o Swagger/OpenAPI.
     *
     * @return Configuração do OpenAPI
     */
    @Bean
    public OpenAPI boltEnergyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bolt Energy API")
                        .description("API da aplicação Bolt Energy - Documentação interativa")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("contato@boltenergy.com")));
    }
}
