package com.boltenergy.repository;

import com.boltenergy.model.entity.RalieMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RalieMetadataRepository extends JpaRepository<RalieMetadataEntity, Long> {
    
    default RalieMetadataEntity findFirstByOrderByLastDownloadTimeDesc() {
        RalieMetadataEntity result = findTopByOrderByLastDownloadTimeDesc();

        return result;
    }
    
    RalieMetadataEntity findTopByOrderByLastDownloadTimeDesc();
}
