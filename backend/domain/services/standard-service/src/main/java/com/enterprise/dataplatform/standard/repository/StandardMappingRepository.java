package com.enterprise.dataplatform.standard.repository;

import com.enterprise.dataplatform.standard.domain.entity.StandardMapping;
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
 * 标准映射Repository
 */
@Repository
public interface StandardMappingRepository extends JpaRepository<StandardMapping, Long>, JpaSpecificationExecutor<StandardMapping> {

    /**
     * 根据数据标准ID查询映射
     */
    List<StandardMapping> findByDataStandardId(Long standardId);

    /**
     * 根据资产ID查询映射
     */
    List<StandardMapping> findByAssetId(String assetId);

    /**
     * 根据资产ID和字段名称查询映射
     */
    Optional<StandardMapping> findByAssetIdAndFieldName(String assetId, String fieldName);

    /**
     * 根据映射状态查询
     */
    List<StandardMapping> findByMappingStatus(String mappingStatus);

    /**
     * 根据数据标准ID和映射状态查询
     */
    List<StandardMapping> findByDataStandardIdAndMappingStatus(Long standardId, String mappingStatus);

    /**
     * 根据资产类型查询映射
     */
    List<StandardMapping> findByAssetType(String assetType);

    /**
     * 检查映射是否存在
     */
    boolean existsByAssetIdAndFieldName(String assetId, String fieldName);

    /**
     * 分页查询映射
     */
    @Query("SELECT sm FROM StandardMapping sm WHERE " +
           "(:standardId IS NULL OR sm.dataStandard.id = :standardId) AND " +
           "(:assetId IS NULL OR sm.assetId = :assetId) AND " +
           "(:mappingStatus IS NULL OR sm.mappingStatus = :mappingStatus)")
    Page<StandardMapping> searchMappings(
            @Param("standardId") Long standardId,
            @Param("assetId") String assetId,
            @Param("mappingStatus") String mappingStatus,
            Pageable pageable);

    /**
     * 统计各类映射状态的数量
     */
    @Query("SELECT sm.mappingStatus, COUNT(sm) FROM StandardMapping sm GROUP BY sm.mappingStatus")
    List<Object[]> countByMappingStatus();

    /**
     * 根据映射来源查询
     */
    List<StandardMapping> findByMappingSource(String mappingSource);

    /**
     * 查询待审批的映射
     */
    @Query("SELECT sm FROM StandardMapping sm WHERE sm.mappingStatus = 'PENDING' ORDER BY sm.createTime DESC")
    Page<StandardMapping> findPendingMappings(Pageable pageable);

    /**
     * 批量查询资产映射
     */
    List<StandardMapping> findByAssetIdIn(List<String> assetIds);

    /**
     * 查询关键字段映射
     */
    @Query("SELECT sm FROM StandardMapping sm WHERE sm.isKeyField = true AND sm.assetId = :assetId")
    List<StandardMapping> findKeyFieldMappings(@Param("assetId") String assetId);
}
