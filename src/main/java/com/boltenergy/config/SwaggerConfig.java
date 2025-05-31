package com.boltenergy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Swagger/OpenAPI para documentação da API.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("bolt-energy-api")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public OpenAPI boltEnergyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bolt Energy API")
                        .description("Documentação interativa da API da Bolt Energy")
                        .version("1.0.0"));
    }
}
