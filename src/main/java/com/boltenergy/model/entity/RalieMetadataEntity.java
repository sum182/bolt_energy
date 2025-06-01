package com.boltenergy.model.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


@Data
@ToString
@Entity
@Table(name = "ralie_metadata")
public class RalieMetadataEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "etag", length = 255)
    private String etag;
    
    @Column(name = "last_modified", length = 255)
    private String lastModified;
    
    @Column(name = "last_downloaded_file", length = 1000)
    private String lastDownloadedFile;
    
    @Column(name = "last_download_time")
    private LocalDateTime lastDownloadTime;
    
    @Column(name = "file_size")
    private long fileSize;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public void update(String etag, String lastModified, String downloadedFilePath, long fileSize) {
        this.etag = etag;
        this.lastModified = lastModified;
        this.lastDownloadedFile = downloadedFilePath;
        this.lastDownloadTime = LocalDateTime.now();
        this.fileSize = fileSize;
    }
}
