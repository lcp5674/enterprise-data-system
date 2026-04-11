package com.enterprise.dataplatform.metadata.repository;

import com.enterprise.dataplatform.metadata.entity.MetadataObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MetadataObject entity
 */
@Repository
public interface MetadataObjectRepository extends JpaRepository<MetadataObject, Long>,
        JpaSpecificationExecutor<MetadataObject> {

    Optional<MetadataObject> findByObjectId(String objectId);

    List<MetadataObject> findByDomainCode(String domainCode);

    List<MetadataObject> findByObjectType(String objectType);

    long countByDomainCode(String domainCode);

    @Query("SELECT COUNT(m) FROM MetadataObject m WHERE m.objectType = :objectType")
    long countByObjectType(@Param("objectType") String objectType);

    @Query("SELECT COUNT(m) FROM MetadataObject m WHERE m.sensitivity = :sensitivity")
    long countBySensitivity(@Param("sensitivity") String sensitivity);

    boolean existsByObjectId(String objectId);

    void deleteByObjectId(String objectId);
}
