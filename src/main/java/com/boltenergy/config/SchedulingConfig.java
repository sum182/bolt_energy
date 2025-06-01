package com.boltenergy.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(RalieSchedulingProperties.class)
@ConditionalOnProperty(prefix = "ralie.schedule", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
