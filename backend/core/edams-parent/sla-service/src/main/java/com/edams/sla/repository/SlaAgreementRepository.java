package com.edams.sla.repository;

import com.edams.sla.entity.SlaAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlaAgreementRepository extends JpaRepository<SlaAgreement, Long>, JpaSpecificationExecutor<SlaAgreement> {
    
    List<SlaAgreement> findByServiceName(String serviceName);
    
    List<SlaAgreement> findByServiceType(String serviceType);
    
    List<SlaAgreement> findByOwnerId(Long ownerId);
    
    List<SlaAgreement> findByStatus(String status);
    
    @Query("SELECT s FROM SlaAgreement s WHERE s.endTime < :currentTime AND s.status = 'ACTIVE'")
    List<SlaAgreement> findExpiredAgreements(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT s FROM SlaAgreement s WHERE s.serviceName = :serviceName AND s.metricType = :metricType AND s.status = 'ACTIVE'")
    List<SlaAgreement> findActiveAgreementsByServiceAndMetric(
            @Param("serviceName") String serviceName,
            @Param("metricType") String metricType);
}