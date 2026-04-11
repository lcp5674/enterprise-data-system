package com.enterprise.edams.datasource.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.datasource.entity.DatasourceSyncTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据源同步任务Mapper接口
 */
@Mapper
public interface DatasourceSyncTaskRepository extends BaseMapper<DatasourceSyncTask> {

    /**
     * 分页查询同步任务
     *
     * @param page         分页参数
     * @param datasourceId  数据源ID
     * @param status       状态
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return 分页结果
     */
    IPage<DatasourceSyncTask> selectPageList(Page<DatasourceSyncTask> page,
                                             @Param("datasourceId") Long datasourceId,
                                             @Param("status") String status,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 根据数据源ID查询最近的任务
     *
     * @param datasourceId 数据源ID
     * @param limit        数量
     * @return 任务列表
     */
    List<DatasourceSyncTask> selectRecentByDatasourceId(@Param("datasourceId") Long datasourceId, @Param("limit") Integer limit);

    /**
     * 查询正在运行的任务
     *
     * @return 任务列表
     */
    List<DatasourceSyncTask> selectRunningTasks();

    /**
     * 查询待执行的任务
     *
     * @param limit 数量
     * @return 任务列表
     */
    List<DatasourceSyncTask> selectPendingTasks(@Param("limit") Integer limit);

    /**
     * 查询失败的任务
     *
     * @param days 天数
     * @return 任务列表
     */
    List<DatasourceSyncTask> selectFailedTasks(@Param("days") Integer days);
}
