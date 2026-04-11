package com.enterprise.edams.lifecycle.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.lifecycle.entity.DataLifecycle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 数据生命周期Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface LifecycleMapper extends BaseMapper<DataLifecycle> {

    @Select("SELECT * FROM data_lifecycle WHERE data_asset_id = #{dataAssetId} AND deleted = 0")
    DataLifecycle findByDataAssetId(@Param("dataAssetId") Long dataAssetId);

    @Select("SELECT * FROM data_lifecycle WHERE data_asset_name LIKE CONCAT('%', #{keyword}, '%') AND deleted = 0")
    IPage<DataLifecycle> searchByKeyword(Page<DataLifecycle> page, @Param("keyword") String keyword);

    @Select("SELECT * FROM data_lifecycle WHERE current_stage = #{stage} AND deleted = 0")
    IPage<DataLifecycle> findByStage(Page<DataLifecycle> page, @Param("stage") String stage);
}