package com.enterprise.edams.lifecycle.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.lifecycle.entity.LifecycleStage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 生命周期阶段Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface LifecycleStageMapper extends BaseMapper<LifecycleStage> {

    @Select("SELECT * FROM lifecycle_stage WHERE stage_code = #{stageCode} AND deleted = 0 AND enabled = 1")
    LifecycleStage findByStageCode(@Param("stageCode") String stageCode);

    @Select("SELECT * FROM lifecycle_stage WHERE next_stage_code = #{stageCode} AND deleted = 0 AND enabled = 1")
    LifecycleStage findByPreviousStage(@Param("stageCode") String stageCode);
}