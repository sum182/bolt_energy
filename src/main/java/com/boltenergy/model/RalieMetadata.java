package com.boltenergy.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Data
public class RalieMetadata {
    private String etag;
    private String lastModified;
    private String lastDownloadedFile;
    private LocalDateTime lastDownloadTime;
    private long fileSize;
    
    public void update(String etag, String lastModified, String downloadedFilePath, long fileSize) {
        this.etag = etag;
        this.lastModified = lastModified;
        this.lastDownloadedFile = downloadedFilePath;
        this.lastDownloadTime = LocalDateTime.now();
        this.fileSize = fileSize;
    }
    

    public String getFormattedLastDownloadTime() {
        if (lastDownloadTime == null) return "Never";
        return lastDownloadTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
