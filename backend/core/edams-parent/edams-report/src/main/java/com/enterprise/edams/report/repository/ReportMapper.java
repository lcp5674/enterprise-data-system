package com.enterprise.edams.report.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.report.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 报表Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    /**
     * 根据报表编码查询
     */
    @Select("SELECT * FROM edams_report WHERE report_code = #{reportCode} AND deleted = 0 LIMIT 1")
    Report selectByReportCode(@Param("reportCode") String reportCode);

    /**
     * 检查报表编码是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_report WHERE report_code = #{reportCode} AND deleted = 0")
    int existsByReportCode(@Param("reportCode") String reportCode);

    /**
     * 检查报表名称是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_report WHERE report_name = #{reportName} AND deleted = 0")
    int existsByReportName(@Param("reportName") String reportName);

    /**
     * 根据报表类型查询
     */
    @Select("SELECT * FROM edams_report WHERE report_type = #{reportType} AND deleted = 0 ORDER BY created_time DESC")
    List<Report> selectByReportType(@Param("reportType") String reportType);

    /**
     * 根据创建人查询
     */
    @Select("SELECT * FROM edams_report WHERE creator_id = #{creatorId} AND deleted = 0 ORDER BY created_time DESC")
    List<Report> selectByCreatorId(@Param("creatorId") Long creatorId);

    /**
     * 根据模板ID查询
     */
    @Select("SELECT * FROM edams_report WHERE template_id = #{templateId} AND deleted = 0 ORDER BY created_time DESC")
    List<Report> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 更新报表状态
     */
    @Update("UPDATE edams_report SET status = #{status}, updated_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新最后执行信息
     */
    @Update("UPDATE edams_report SET last_execute_time = #{executeTime}, last_execute_status = #{executeStatus}, " +
            "execute_count = execute_count + 1, updated_time = NOW() WHERE id = #{id}")
    int updateExecuteInfo(@Param("id") Long id, @Param("executeTime") String executeTime, @Param("executeStatus") String executeStatus);

    /**
     * 增加浏览次数
     */
    @Update("UPDATE edams_report SET view_count = view_count + 1, updated_time = NOW() WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 统计报表数量
     */
    @Select("SELECT COUNT(1) FROM edams_report WHERE deleted = 0")
    int countTotal();

    /**
     * 统计各状态报表数量
     */
    @Select("SELECT COUNT(1) FROM edams_report WHERE status = #{status} AND deleted = 0")
    int countByStatus(@Param("status") Integer status);
}
