package com.boltenergy.service;

import com.boltenergy.model.RalieMetadata;
import com.boltenergy.model.entity.RalieMetadataEntity;
import com.boltenergy.repository.RalieMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RalieMetadataDbServiceTest {

    @Mock
    private RalieMetadataRepository metadataRepository;

    @InjectMocks
    private RalieMetadataDbService metadataService;

    private RalieMetadataEntity testEntity;
    private RalieMetadata testMetadata;

    @BeforeEach
    void setUp() {
        testEntity = new RalieMetadataEntity();
        testEntity.setId(1L);
        testEntity.setEtag("test-etag");
        testEntity.setLastModified("test-last-modified");
        testEntity.setLastDownloadedFile("/path/to/file.csv");
        testEntity.setLastDownloadTime(LocalDateTime.now());
        testEntity.setFileSize(1024L);

        testMetadata = new RalieMetadata();
        testMetadata.setEtag("test-etag");
        testMetadata.setLastModified("test-last-modified");
        testMetadata.setLastDownloadedFile("/path/to/file.csv");
        testMetadata.setFileSize(1024L);
    }

    @Test
    void init_ShouldInitializeService() {
        Path testPath = Path.of("/test/path");
        metadataService.init(testPath);
        assertDoesNotThrow(() -> metadataService.init(testPath));
    }

    @Test
    void loadMetadata_WhenNoMetadataExists_ShouldReturnNewMetadata() {
        when(metadataRepository.findFirstByOrderByLastDownloadTimeDesc()).thenReturn(null);
        
        RalieMetadata result = metadataService.loadMetadata();
        
        assertNotNull(result);
        assertNull(result.getEtag());
        verify(metadataRepository).findFirstByOrderByLastDownloadTimeDesc();
    }

    @Test
    void loadMetadata_WhenMetadataExists_ShouldReturnExistingMetadata() {
        when(metadataRepository.findFirstByOrderByLastDownloadTimeDesc()).thenReturn(testEntity);
        
        RalieMetadata result = metadataService.loadMetadata();
        
        assertNotNull(result);
        assertEquals(testEntity.getEtag(), result.getEtag());
        assertEquals(testEntity.getLastModified(), result.getLastModified());
        assertEquals(testEntity.getLastDownloadedFile(), result.getLastDownloadedFile());
        assertEquals(testEntity.getFileSize(), result.getFileSize());
        verify(metadataRepository).findFirstByOrderByLastDownloadTimeDesc();
    }

    @Test
    void saveMetadata_WhenNoExistingMetadata_ShouldCreateNewEntity() {
        when(metadataRepository.findFirstByOrderByLastDownloadTimeDesc()).thenReturn(null);
        when(metadataRepository.save(any(RalieMetadataEntity.class))).thenReturn(testEntity);
        
        metadataService.saveMetadata(testMetadata);
        
        verify(metadataRepository).findFirstByOrderByLastDownloadTimeDesc();
        verify(metadataRepository).save(any(RalieMetadataEntity.class));
    }

    @Test
    void saveMetadata_WhenExistingMetadata_ShouldUpdateEntity() {
        when(metadataRepository.findFirstByOrderByLastDownloadTimeDesc()).thenReturn(testEntity);
        when(metadataRepository.save(any(RalieMetadataEntity.class))).thenReturn(testEntity);
        
        RalieMetadata updatedMetadata = new RalieMetadata();
        updatedMetadata.setEtag("updated-etag");
        updatedMetadata.setLastModified("updated-last-modified");
        updatedMetadata.setLastDownloadedFile("/updated/path/to/file.csv");
        updatedMetadata.setFileSize(2048L);
        
        metadataService.saveMetadata(updatedMetadata);
        
        verify(metadataRepository).findFirstByOrderByLastDownloadTimeDesc();
        verify(metadataRepository).save(any(RalieMetadataEntity.class));
    }

    @Test
    void saveMetadata_ShouldUpdateCurrentMetadata() {
        when(metadataRepository.findFirstByOrderByLastDownloadTimeDesc()).thenReturn(testEntity);
        when(metadataRepository.save(any(RalieMetadataEntity.class))).thenReturn(testEntity);
        
        metadataService.saveMetadata(testMetadata);
        
        assertNotNull(metadataService.loadMetadata());
    }
}
