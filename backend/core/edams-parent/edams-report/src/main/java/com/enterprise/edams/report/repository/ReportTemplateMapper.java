package com.enterprise.edams.report.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.report.entity.ReportTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 报表模板Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface ReportTemplateMapper extends BaseMapper<ReportTemplate> {

    /**
     * 根据模板编码查询
     */
    @Select("SELECT * FROM edams_report_template WHERE template_code = #{templateCode} AND deleted = 0 LIMIT 1")
    ReportTemplate selectByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 根据模板名称查询
     */
    @Select("SELECT * FROM edams_report_template WHERE template_name = #{templateName} AND deleted = 0 LIMIT 1")
    ReportTemplate selectByTemplateName(@Param("templateName") String templateName);

    /**
     * 检查模板编码是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_report_template WHERE template_code = #{templateCode} AND deleted = 0")
    int existsByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 根据模板类型查询
     */
    @Select("SELECT * FROM edams_report_template WHERE template_type = #{templateType} AND status = 1 AND deleted = 0 ORDER BY created_time DESC")
    List<ReportTemplate> selectByTemplateType(@Param("templateType") String templateType);

    /**
     * 根据创建人查询
     */
    @Select("SELECT * FROM edams_report_template WHERE creator_id = #{creatorId} AND deleted = 0 ORDER BY created_time DESC")
    List<ReportTemplate> selectByCreatorId(@Param("creatorId") Long creatorId);

    /**
     * 更新模板状态
     */
    @Update("UPDATE edams_report_template SET status = #{status}, updated_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 增加使用次数
     */
    @Update("UPDATE edams_report_template SET usage_count = usage_count + 1, updated_time = NOW() WHERE id = #{id}")
    int incrementUsageCount(@Param("id") Long id);

    /**
     * 查询热门模板
     */
    @Select("SELECT * FROM edams_report_template WHERE status = 1 AND deleted = 0 ORDER BY usage_count DESC LIMIT #{limit}")
    List<ReportTemplate> selectHotTemplates(@Param("limit") Integer limit);

    /**
     * 统计模板数量
     */
    @Select("SELECT COUNT(1) FROM edams_report_template WHERE deleted = 0")
    int countTotal();

    /**
     * 模糊搜索模板
     */
    @Select("SELECT * FROM edams_report_template WHERE (template_name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "description LIKE CONCAT('%', #{keyword}, '%')) AND status = 1 AND deleted = 0 ORDER BY usage_count DESC LIMIT #{limit}")
    List<ReportTemplate> searchByKeyword(@Param("keyword") String keyword, @Param("limit") Integer limit);
}
