package com.boltenergy.service;

import com.boltenergy.model.RalieMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service to manage RALIE metadata persistence.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RalieMetadataService {
    
    private static final String METADATA_FILENAME = "ralie_metadata.json";
    private final ObjectMapper objectMapper;
    private Path metadataPath;
    
    /**
     * Initializes the metadata service.
     * @param basePath Base path for storing metadata file (typically the downloads directory)
     */
    public void init(Path basePath) {
        try {
            // Use the provided basePath (downloads directory)
            if (basePath == null) {
                throw new IllegalArgumentException("Base path cannot be null");
            }
            
            // Ensure the downloads directory exists
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
            }
            
            this.metadataPath = basePath.resolve(METADATA_FILENAME);
            log.info("Metadata file will be stored at: {}", metadataPath);
            
            // Ensure the file exists
            if (!Files.exists(metadataPath)) {
                saveMetadata(new RalieMetadata());
            } else {
                // Just try to read to ensure the file is accessible
                loadMetadata();
            }
        } catch (Exception e) {
            log.error("Failed to initialize metadata path: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize metadata storage", e);
        }
    }
    
    /**
     * Loads metadata from file or creates a new instance if not found.
     */
    public RalieMetadata loadMetadata() {
        if (metadataPath == null) {
            log.warn("Metadata path not initialized, returning new metadata instance");
            return new RalieMetadata();
        }
        
        if (!Files.exists(metadataPath)) {
            log.info("No existing metadata file found at: {}, creating new instance", metadataPath);
            return new RalieMetadata();
        }
        
        try (var reader = Files.newBufferedReader(metadataPath)) {
            RalieMetadata metadata = objectMapper.readValue(reader, RalieMetadata.class);
            log.info("Successfully loaded metadata from: {}", metadataPath);
            return metadata;
        } catch (IOException e) {
            log.error("Failed to load metadata from {}: {}", metadataPath, e.getMessage(), e);
            return new RalieMetadata();
        }
    }
    
    /**
     * Saves metadata to file.
     */
    public void saveMetadata(RalieMetadata metadata) {
        if (metadataPath == null) {
            log.warn("Metadata path not initialized, cannot save");
            return;
        }
        
        try {
            // Ensure parent directory exists
            Files.createDirectories(metadataPath.getParent());
            
            // Write directly to the target file using try-with-resources to ensure proper resource cleanup
            try (var writer = Files.newBufferedWriter(metadataPath)) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, metadata);
                log.debug("Metadata saved successfully to: {}", metadataPath);
            }
        } catch (IOException e) {
            log.error("Failed to save metadata to {}: {}", metadataPath, e.getMessage(), e);
        }
    }
}
