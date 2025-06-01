package com.boltenergy.service;

import com.boltenergy.model.RalieMetadata;
import java.nio.file.Path;

public interface RalieMetadataService {
    void init(Path basePath);
    RalieMetadata loadMetadata();
    void saveMetadata(RalieMetadata metadata);
}
