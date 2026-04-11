package com.edams.sandbox.repository;

import com.edams.sandbox.entity.Sandbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SandboxRepository extends JpaRepository<Sandbox, Long>, JpaSpecificationExecutor<Sandbox> {
    
    List<Sandbox> findByOwnerId(Long ownerId);
    
    List<Sandbox> findByStatus(String status);
    
    List<Sandbox> findBySandboxType(String sandboxType);
    
    @Query("SELECT s FROM Sandbox s WHERE s.expireTime < :currentTime AND s.status != 'EXPIRED'")
    List<Sandbox> findExpiredSandboxes(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT s FROM Sandbox s WHERE s.ownerId = :ownerId AND s.status != 'EXPIRED' ORDER BY s.createdTime DESC")
    List<Sandbox> findActiveSandboxesByOwner(@Param("ownerId") Long ownerId);
}