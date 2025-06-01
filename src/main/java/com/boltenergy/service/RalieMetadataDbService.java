package com.boltenergy.service;

import com.boltenergy.model.RalieMetadata;
import com.boltenergy.model.entity.RalieMetadataEntity;
import com.boltenergy.repository.RalieMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RalieMetadataDbService implements RalieMetadataService {
    
    private final RalieMetadataRepository metadataRepository;
    private RalieMetadataEntity currentMetadata;
    
    @Override
    public void init(Path basePath) {
        // Mantido para compatibilidade
    }
    
    @Override
    @Transactional(readOnly = true)
    public RalieMetadata loadMetadata() {
        RalieMetadataEntity latestMetadata = metadataRepository.findFirstByOrderByLastDownloadTimeDesc();
        this.currentMetadata = latestMetadata;
        return latestMetadata != null ? convertToDto(latestMetadata) : new RalieMetadata();
    }
    
    @Override
    @Transactional
    public void saveMetadata(RalieMetadata metadata) {
        RalieMetadataEntity entityToSave = Optional.ofNullable(metadataRepository.findFirstByOrderByLastDownloadTimeDesc())
            .orElse(new RalieMetadataEntity());
            
        entityToSave.update(
            metadata.getEtag(),
            metadata.getLastModified(),
            metadata.getLastDownloadedFile(),
            metadata.getFileSize()
        );
        
        this.currentMetadata = metadataRepository.save(entityToSave);
    }
    
    private RalieMetadata convertToDto(RalieMetadataEntity entity) {
        RalieMetadata dto = new RalieMetadata();
        dto.setEtag(entity.getEtag());
        dto.setLastModified(entity.getLastModified());
        dto.setLastDownloadedFile(entity.getLastDownloadedFile());
        dto.setLastDownloadTime(entity.getLastDownloadTime());
        dto.setFileSize(entity.getFileSize());
        return dto;
    }
}
