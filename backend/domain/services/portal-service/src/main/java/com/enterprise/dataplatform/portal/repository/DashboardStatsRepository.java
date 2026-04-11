package com.enterprise.dataplatform.portal.repository;

import com.enterprise.dataplatform.portal.entity.DashboardStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Dashboard Stats Repository
 */
@Repository
public interface DashboardStatsRepository extends JpaRepository<DashboardStats, Long> {

    Optional<DashboardStats> findByUserIdAndStatsDate(String userId, LocalDate statsDate);

    Optional<DashboardStats> findTopByUserIdOrderByStatsDateDesc(String userId);
}
