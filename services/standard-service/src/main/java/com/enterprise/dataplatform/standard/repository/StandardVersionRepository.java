package com.enterprise.dataplatform.standard.repository;

import com.enterprise.dataplatform.standard.domain.entity.StandardVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 标准版本Repository
 */
@Repository
public interface StandardVersionRepository extends JpaRepository<StandardVersion, Long> {

    /**
     * 根据数据标准ID查询版本历史
     */
    List<StandardVersion> findByDataStandardIdOrderByVersionNoDesc(Long standardId);

    /**
     * 根据数据标准ID和版本号查询
     */
    Optional<StandardVersion> findByDataStandardIdAndVersionNo(Long standardId, Integer versionNo);

    /**
     * 获取最新版本
     */
    @Query("SELECT sv FROM StandardVersion sv WHERE sv.dataStandard.id = :standardId ORDER BY sv.versionNo DESC LIMIT 1")
    Optional<StandardVersion> findLatestVersion(@Param("standardId") Long standardId);

    /**
     * 获取当前激活版本
     */
    @Query("SELECT sv FROM StandardVersion sv WHERE sv.dataStandard.id = :standardId AND sv.status = 'ACTIVE'")
    Optional<StandardVersion> findActiveVersion(@Param("standardId") Long standardId);

    /**
     * 分页查询版本历史
     */
    Page<StandardVersion> findByDataStandardId(Long standardId, Pageable pageable);

    /**
     * 根据变更类型查询
     */
    List<StandardVersion> findByChangeType(String changeType);

    /**
     * 查询待审批版本
     */
    @Query("SELECT sv FROM StandardVersion sv WHERE sv.approvalStatus = 'PENDING' ORDER BY sv.createTime DESC")
    Page<StandardVersion> findPendingApprovals(Pageable pageable);
}
