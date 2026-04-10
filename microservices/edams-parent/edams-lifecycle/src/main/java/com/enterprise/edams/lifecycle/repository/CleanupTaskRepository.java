package com.enterprise.edams.lifecycle.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.lifecycle.entity.CleanupTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 清理任务数据访问层
 * 
 * @author EDAMS Team
 */
@Mapper
public interface CleanupTaskRepository extends BaseMapper<CleanupTask> {

    /**
     * 查询启用的任务
     */
    @Select("SELECT * FROM lc_cleanup_task WHERE enabled = true AND deleted = false")
    List<CleanupTask> findEnabledTasks();

    /**
     * 根据业务类型查询任务
     */
    @Select("SELECT * FROM lc_cleanup_task WHERE business_type = #{businessType} AND enabled = true AND deleted = false")
    List<CleanupTask> findByBusinessType(@Param("businessType") String businessType);
}
