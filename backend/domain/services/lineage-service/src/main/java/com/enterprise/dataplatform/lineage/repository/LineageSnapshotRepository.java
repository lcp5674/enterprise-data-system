package com.enterprise.dataplatform.lineage.repository;

import com.enterprise.dataplatform.lineage.domain.entity.LineageSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LineageSnapshot entity
 */
@Repository
public interface LineageSnapshotRepository extends JpaRepository<LineageSnapshot, String> {

    /**
     * Find by snapshot name
     */
    Optional<LineageSnapshot> findBySnapshotName(String snapshotName);

    /**
     * Find latest snapshot
     */
    Optional<LineageSnapshot> findTopByOrderBySnapshotTimeDesc();

    /**
     * Find snapshots by time range
     */
    List<LineageSnapshot> findBySnapshotTimeBetweenOrderBySnapshotTimeDesc(
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find by status
     */
    List<LineageSnapshot> findByStatusOrderBySnapshotTimeDesc(String status);
}
