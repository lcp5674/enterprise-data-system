package com.enterprise.edams.lifecycle.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.lifecycle.entity.LifecyclePolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 生命周期策略Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface LifecyclePolicyMapper extends BaseMapper<LifecyclePolicy> {

    @Select("SELECT * FROM lifecycle_policy WHERE policy_code = #{policyCode} AND deleted = 0 AND enabled = 1")
    LifecyclePolicy findByPolicyCode(@Param("policyCode") String policyCode);

    @Select("SELECT * FROM lifecycle_policy WHERE data_asset_type = #{assetType} AND deleted = 0 AND enabled = 1")
    IPage<LifecyclePolicy> findByAssetType(Page<LifecyclePolicy> page, @Param("assetType") String assetType);
}