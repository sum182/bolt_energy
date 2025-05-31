package com.boltenergy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração para o agendamento do serviço RALIE.
 */
@ConfigurationProperties(prefix = "ralie.schedule")
public class RalieSchedulingProperties {
    
    /**
     * Expressão cron para agendamento do job.
     * Configurado no application.yml
     */
    private String cron;


    /**
     * Habilita ou desabilita o agendamento automático.
     */
    private boolean enabled = true;
    
    /**
     * Nome do job para fins de log.
     */
    private String jobName = "RALIE Download Job";
    
    // Getters e Setters
    
    public String getCron() {
        return cron;
    }
    
    public void setCron(String cron) {
        this.cron = cron;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getJobName() {
        return jobName;
    }
    
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}
