package com.boltenergy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Metadata for RALIE file downloads.
 * Stores ETag, Last-Modified, and other relevant information.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RalieMetadata {
    private String etag;
    private String lastModified;
    private String lastDownloadedFile;
    private LocalDateTime lastDownloadTime;
    private long fileSize;
    
    /**
     * Updates the metadata with new values.
     */
    public void update(String etag, String lastModified, String downloadedFilePath, long fileSize) {
        this.etag = etag;
        this.lastModified = lastModified;
        this.lastDownloadedFile = downloadedFilePath;
        this.lastDownloadTime = LocalDateTime.now();
        this.fileSize = fileSize;
    }
    
    /**
     * @return Formatted string of the last download time
     */
    public String getFormattedLastDownloadTime() {
        if (lastDownloadTime == null) return "Never";
        return lastDownloadTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
