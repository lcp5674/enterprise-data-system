package com.enterprise.dataplatform.ethics.repository;

import com.enterprise.dataplatform.ethics.domain.entity.EthicsFramework;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EthicsFrameworkRepository extends JpaRepository<EthicsFramework, Long>, JpaSpecificationExecutor<EthicsFramework> {

    Optional<EthicsFramework> findByFrameworkCode(String frameworkCode);

    boolean existsByFrameworkCode(String frameworkCode);

    List<EthicsFramework> findByEnabled(Boolean enabled);

    List<EthicsFramework> findByStatus(String status);

    @Query("SELECT ef FROM EthicsFramework ef WHERE " +
           "(:status IS NULL OR ef.status = :status) AND " +
           "(:category IS NULL OR ef.category = :category) AND " +
           "(:enabled IS NULL OR ef.enabled = :enabled) AND " +
           "(:keyword IS NULL OR ef.frameworkName LIKE %:keyword% OR ef.frameworkCode LIKE %:keyword% OR ef.description LIKE %:keyword%)")
    Page<EthicsFramework> searchFrameworks(
            @Param("status") String status,
            @Param("category") String category,
            @Param("enabled") Boolean enabled,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT ef FROM EthicsFramework ef WHERE ef.tags LIKE %:tag%")
    List<EthicsFramework> findByTagsContaining(@Param("tag") String tag);

    @Query("SELECT COUNT(ef) FROM EthicsFramework ef WHERE ef.status = :status")
    long countByStatus(@Param("status") String status);
}
