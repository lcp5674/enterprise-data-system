package com.enterprise.edams.lifecycle.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.lifecycle.entity.ArchivePolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 归档策略数据访问层
 * 
 * @author EDAMS Team
 */
@Mapper
public interface ArchivePolicyRepository extends BaseMapper<ArchivePolicy> {

    /**
     * 查询启用的策略
     */
    @Select("SELECT * FROM lc_archive_policy WHERE enabled = true AND deleted = false ORDER BY priority DESC")
    List<ArchivePolicy> findEnabledPolicies();

    /**
     * 根据业务类型查询策略
     */
    @Select("SELECT * FROM lc_archive_policy WHERE business_type = #{businessType} AND enabled = true AND deleted = false")
    List<ArchivePolicy> findByBusinessType(@Param("businessType") String businessType);

    /**
     * 根据编码查询策略
     */
    @Select("SELECT * FROM lc_archive_policy WHERE code = #{code} AND deleted = false")
    ArchivePolicy findByCode(@Param("code") String code);
}
