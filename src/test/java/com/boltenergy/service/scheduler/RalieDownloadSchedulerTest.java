package com.boltenergy.service.scheduler;

import com.boltenergy.config.RalieSchedulingProperties;
import com.boltenergy.service.AneelRalieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe RalieDownloadScheduler.
 */
@ExtendWith(MockitoExtension.class)
class RalieDownloadSchedulerTest {

    @Mock
    private AneelRalieService aneelRalieService;

    @Mock
    private RalieSchedulingProperties schedulingProperties;

    @InjectMocks
    private RalieDownloadScheduler scheduler;

    private static final String JOB_NAME = "Test Scheduler";
    private static final String FILE_PATH = "/path/to/downloaded/file.csv";

    @BeforeEach
    void setUp() {
        // Configuração comum que será usada em todos os testes
    }

    @Test
    void scheduledDownload_WhenEnabled_ShouldDownloadFile() {
        // Arrange
        when(schedulingProperties.isEnabled()).thenReturn(true);
        when(schedulingProperties.getJobName()).thenReturn(JOB_NAME);
        when(aneelRalieService.downloadRalieCsv()).thenReturn(FILE_PATH);

        // Act
        scheduler.scheduledDownload();

        // Assert
        verify(aneelRalieService, times(1)).downloadRalieCsv();
        verify(schedulingProperties, times(1)).isEnabled();
        verify(schedulingProperties, times(1)).getJobName();
    }

    @Test
    void scheduledDownload_WhenDisabled_ShouldNotDownloadFile() {
        // Arrange
        when(schedulingProperties.isEnabled()).thenReturn(false);

        // Act
        scheduler.scheduledDownload();

        // Assert
        verify(aneelRalieService, never()).downloadRalieCsv();
        verify(schedulingProperties, times(1)).isEnabled();
        verify(schedulingProperties, never()).getJobName();
    }

    @Test
    void scheduledDownload_WhenExceptionThrown_ShouldLogError() {
        // Arrange
        String errorMessage = "Erro durante o download";
        when(schedulingProperties.isEnabled()).thenReturn(true);
        when(schedulingProperties.getJobName()).thenReturn(JOB_NAME);
        when(aneelRalieService.downloadRalieCsv()).thenThrow(new RuntimeException(errorMessage));

        // Act
        scheduler.scheduledDownload();

        // Assert
        verify(aneelRalieService, times(1)).downloadRalieCsv();
        verify(schedulingProperties, times(1)).isEnabled();
        verify(schedulingProperties, times(1)).getJobName();
    }
}
