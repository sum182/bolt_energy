package com.boltenergy.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Data
@Slf4j
@Validated
@ConfigurationProperties(prefix = "webclient")
public class WebClientProperties {
    
    public WebClientProperties() {
        log.info("Configurações do WebClient carregadas: {}", this);
    }
    private DataSize maxInMemorySize = DataSize.ofMegabytes(50); // 50MB
    
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration connectTimeout = Duration.ofSeconds(300); // 5 minutos
    
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration responseTimeout = Duration.ofSeconds(300); // 5 minutos
    
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration readTimeout = Duration.ofSeconds(600); // 10 minutos
    
    private DataSize bufferSize = DataSize.ofMegabytes(1); // 1MB
}
