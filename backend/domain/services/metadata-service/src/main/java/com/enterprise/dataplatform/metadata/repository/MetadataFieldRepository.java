package com.enterprise.dataplatform.metadata.repository;

import com.enterprise.dataplatform.metadata.entity.MetadataField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MetadataField entity
 */
@Repository
public interface MetadataFieldRepository extends JpaRepository<MetadataField, Long> {

    List<MetadataField> findByObjectId(String objectId);

    void deleteByObjectId(String objectId);

    long countByObjectId(String objectId);
}
