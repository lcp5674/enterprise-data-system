package com.enterprise.dataplatform.metadata.repository;

import com.enterprise.dataplatform.metadata.entity.MetadataTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for MetadataTag entity
 */
@Repository
public interface MetadataTagRepository extends JpaRepository<MetadataTag, Long> {

    Optional<MetadataTag> findByTagName(String tagName);

    boolean existsByTagName(String tagName);
}
