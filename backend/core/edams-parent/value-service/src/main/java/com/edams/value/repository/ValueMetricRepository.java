package com.edams.value.repository;

import com.edams.value.entity.ValueMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValueMetricRepository extends JpaRepository<ValueMetric, Long>, JpaSpecificationExecutor<ValueMetric> {
    
    List<ValueMetric> findByMetricType(String metricType);
    
    List<ValueMetric> findByStatus(String status);
    
    List<ValueMetric> findByMetricName(String metricName);
    
    @Query("SELECT m FROM ValueMetric m WHERE m.metricType = :metricType AND m.weight >= :minWeight")
    List<ValueMetric> findMetricsByTypeAndWeight(
            @Param("metricType") String metricType,
            @Param("minWeight") Double minWeight);
    
    @Query("SELECT m FROM ValueMetric m WHERE m.metricType = :metricType ORDER BY m.weight DESC")
    List<ValueMetric> findMetricsByTypeSorted(@Param("metricType") String metricType);
}