package com.enterprise.dataplatform.lineage.repository;

import com.enterprise.dataplatform.lineage.domain.entity.LineageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for LineageHistory entity
 */
@Repository
public interface LineageHistoryRepository extends JpaRepository<LineageHistory, String> {

    /**
     * Find by lineage ID
     */
    List<LineageHistory> findByLineageIdOrderByCreatedTimeDesc(String lineageId);

    /**
     * Find by lineage ID with pagination
     */
    Page<LineageHistory> findByLineageId(String lineageId, Pageable pageable);

    /**
     * Find by change type
     */
    List<LineageHistory> findByChangeTypeOrderByCreatedTimeDesc(String changeType);

    /**
     * Find by time range
     */
    List<LineageHistory> findByCreatedTimeBetweenOrderByCreatedTimeDesc(
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find by creator
     */
    List<LineageHistory> findByCreatedByOrderByCreatedTimeDesc(String createdBy);
}
