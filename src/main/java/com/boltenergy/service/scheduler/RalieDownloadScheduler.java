package com.boltenergy.service.scheduler;

import com.boltenergy.config.RalieSchedulingProperties;
import com.boltenergy.service.AneelRalieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RalieDownloadScheduler {

    private final AneelRalieService aneelRalieService;
    private final RalieSchedulingProperties schedulingProperties;
    
    public RalieDownloadScheduler(AneelRalieService aneelRalieService, 
                                 RalieSchedulingProperties schedulingProperties) {
        this.aneelRalieService = aneelRalieService;
        this.schedulingProperties = schedulingProperties;
    }
    
    @Scheduled(cron = "${ralie.schedule.cron}")
    public void scheduledDownload() {
        if (!schedulingProperties.isEnabled()) {
            log.debug("Agendamento de download do RALIE está desabilitado");
            return;
        }
        
        String jobName = schedulingProperties.getJobName();
        log.info("Iniciando {} - Iniciando download agendado do arquivo RALIE", jobName);
        
        try {
            String filePath = aneelRalieService.downloadRalieCsv();
            log.info("{} - Download concluído com sucesso. Arquivo salvo em: {}", jobName, filePath);
        } catch (Exception e) {
            log.error("{} - Erro durante o download agendado do arquivo RALIE: {}", jobName, e.getMessage(), e);
        }
    }
}
