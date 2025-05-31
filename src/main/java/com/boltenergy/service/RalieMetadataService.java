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
     * @param basePath Base path for storing metadata file (not used, kept for backward compatibility)
     */
    public void init(Path basePath) {
        try {
            // Get the project root directory
            String projectRoot = System.getProperty("user.dir");
            Path resourcesPath = Paths.get(projectRoot, "src", "main", "resources");
            
            // Create the resources directory if it doesn't exist
            if (!Files.exists(resourcesPath)) {
                Files.createDirectories(resourcesPath);
            }
            
            this.metadataPath = resourcesPath.resolve(METADATA_FILENAME);
            log.info("Metadata file will be stored at: {}", metadataPath);
            
            // Ensure the file exists
            if (!Files.exists(metadataPath)) {
                saveMetadata(new RalieMetadata());
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
        if (metadataPath == null || !Files.exists(metadataPath)) {
            log.info("No existing metadata found, creating new instance");
            return new RalieMetadata();
        }
        
        try {
            RalieMetadata metadata = objectMapper.readValue(metadataPath.toFile(), RalieMetadata.class);
            log.info("Loaded metadata from: {}", metadataPath);
            return metadata;
        } catch (IOException e) {
            log.warn("Failed to load metadata from {}: {}", metadataPath, e.getMessage());
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
            
            // Write to a temp file first, then rename for atomicity
            Path tempFile = Files.createTempFile(metadataPath.getParent(), "ralie_metadata", ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), metadata);
            
            // Replace the old file atomically
            Files.move(tempFile, metadataPath, 
                     java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                     java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            
            log.debug("Metadata saved to: {}", metadataPath);
        } catch (IOException e) {
            log.error("Failed to save metadata to {}: {}", metadataPath, e.getMessage(), e);
        }
    }
}
