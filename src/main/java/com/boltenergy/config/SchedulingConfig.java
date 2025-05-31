package com.boltenergy.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuração do agendamento de tarefas.
 * Configura o agendamento baseado na propriedade 'ralie.schedule.enabled'.
 * A anotação @EnableScheduling está na classe principal da aplicação.
 */
@Configuration
@EnableConfigurationProperties(RalieSchedulingProperties.class)
@ConditionalOnProperty(prefix = "ralie.schedule", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
    // Configuração do agendamento
}
