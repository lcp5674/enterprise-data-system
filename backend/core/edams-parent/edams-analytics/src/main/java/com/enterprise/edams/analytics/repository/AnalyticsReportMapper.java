package com.enterprise.edams.analytics.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.analytics.entity.AnalyticsReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 分析报告Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface AnalyticsReportMapper extends BaseMapper<AnalyticsReport> {

    /**
     * 根据报告编码查询
     */
    @Select("SELECT * FROM edams_analytics_report WHERE report_code = #{reportCode} AND deleted = 0 LIMIT 1")
    AnalyticsReport selectByReportCode(@Param("reportCode") String reportCode);

    /**
     * 检查报告编码是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_analytics_report WHERE report_code = #{reportCode} AND deleted = 0")
    int existsByReportCode(@Param("reportCode") String reportCode);

    /**
     * 检查报告名称是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_analytics_report WHERE report_name = #{reportName} AND deleted = 0")
    int existsByReportName(@Param("reportName") String reportName);

    /**
     * 根据报告类型查询
     */
    @Select("SELECT * FROM edams_analytics_report WHERE report_type = #{reportType} AND deleted = 0 ORDER BY created_time DESC")
    List<AnalyticsReport> selectByReportType(@Param("reportType") String reportType);

    /**
     * 根据创建人查询
     */
    @Select("SELECT * FROM edams_analytics_report WHERE creator_id = #{creatorId} AND deleted = 0 ORDER BY created_time DESC")
    List<AnalyticsReport> selectByCreatorId(@Param("creatorId") Long creatorId);

    /**
     * 根据数据源查询
     */
    @Select("SELECT * FROM edams_analytics_report WHERE datasource_id = #{datasourceId} AND deleted = 0 ORDER BY created_time DESC")
    List<AnalyticsReport> selectByDatasourceId(@Param("datasourceId") Long datasourceId);

    /**
     * 更新报告状态
     */
    @Update("UPDATE edams_analytics_report SET status = #{status}, updated_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新最后执行信息
     */
    @Update("UPDATE edams_analytics_report SET last_execute_time = #{executeTime}, last_execute_status = #{executeStatus}, " +
            "execute_count = execute_count + 1, updated_time = NOW() WHERE id = #{id}")
    int updateExecuteInfo(@Param("id") Long id, @Param("executeTime") String executeTime, @Param("executeStatus") String executeStatus);

    /**
     * 增加浏览次数
     */
    @Update("UPDATE edams_analytics_report SET view_count = view_count + 1, updated_time = NOW() WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 根据访问权限查询
     */
    @Select("SELECT * FROM edams_analytics_report WHERE access_level = #{accessLevel} AND status = 1 AND deleted = 0 ORDER BY created_time DESC")
    List<AnalyticsReport> selectByAccessLevel(@Param("accessLevel") String accessLevel);

    /**
     * 统计报告总数
     */
    @Select("SELECT COUNT(1) FROM edams_analytics_report WHERE deleted = 0")
    int countTotal();

    /**
     * 统计各状态报告数量
     */
    @Select("SELECT COUNT(1) FROM edams_analytics_report WHERE status = #{status} AND deleted = 0")
    int countByStatus(@Param("status") Integer status);
}
