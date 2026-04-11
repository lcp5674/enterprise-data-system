package com.enterprise.dataplatform.standard.repository;

import com.enterprise.dataplatform.standard.domain.entity.DataStandard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 数据标准Repository
 */
@Repository
public interface DataStandardRepository extends JpaRepository<DataStandard, Long>, JpaSpecificationExecutor<DataStandard> {

    /**
     * 根据标准编码查询
     */
    Optional<DataStandard> findByStandardCode(String standardCode);

    /**
     * 检查标准编码是否存在
     */
    boolean existsByStandardCode(String standardCode);

    /**
     * 根据类别查询标准
     */
    List<DataStandard> findByCategory(String category);

    /**
     * 根据状态查询标准
     */
    List<DataStandard> findByStatus(String status);

    /**
     * 根据类别和状态查询标准
     */
    List<DataStandard> findByCategoryAndStatus(String category, String status);

    /**
     * 分页查询标准
     */
    Page<DataStandard> findByCategory(String category, Pageable pageable);

    /**
     * 分页查询标准（多条件）
     */
    @Query("SELECT ds FROM DataStandard ds WHERE " +
           "(:category IS NULL OR ds.category = :category) AND " +
           "(:status IS NULL OR ds.status = :status) AND " +
           "(:standardType IS NULL OR ds.standardType = :standardType) AND " +
           "(:keyword IS NULL OR ds.standardName LIKE %:keyword% OR ds.standardCode LIKE %:keyword%)")
    Page<DataStandard> searchStandards(
            @Param("category") String category,
            @Param("status") String status,
            @Param("standardType") String standardType,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 获取所有激活的标准
     */
    @Query("SELECT ds FROM DataStandard ds WHERE ds.status = 'ACTIVE' ORDER BY ds.priority, ds.standardCode")
    List<DataStandard> findAllActive();

    /**
     * 根据标准来源查询
     */
    List<DataStandard> findBySource(String source);

    /**
     * 统计各类别的标准数量
     */
    @Query("SELECT ds.category, COUNT(ds) FROM DataStandard ds GROUP BY ds.category")
    List<Object[]> countByCategory();

    /**
     * 统计各状态的标准数量
     */
    @Query("SELECT ds.status, COUNT(ds) FROM DataStandard ds GROUP BY ds.status")
    List<Object[]> countByStatus();

    /**
     * 根据资产ID查询映射的标准
     */
    @Query("SELECT ds FROM DataStandard ds JOIN ds.mappings m WHERE m.assetId = :assetId AND ds.status = 'ACTIVE'")
    List<DataStandard> findMappedStandardsByAssetId(@Param("assetId") String assetId);
}
