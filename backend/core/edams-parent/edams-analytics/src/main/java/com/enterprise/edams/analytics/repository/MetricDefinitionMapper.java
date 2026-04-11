package com.enterprise.edams.analytics.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.analytics.entity.MetricDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 指标定义Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface MetricDefinitionMapper extends BaseMapper<MetricDefinition> {

    /**
     * 根据指标编码查询
     */
    @Select("SELECT * FROM edams_metric_definition WHERE metric_code = #{metricCode} AND deleted = 0 LIMIT 1")
    MetricDefinition selectByMetricCode(@Param("metricCode") String metricCode);

    /**
     * 根据指标名称查询
     */
    @Select("SELECT * FROM edams_metric_definition WHERE metric_name = #{metricName} AND deleted = 0 LIMIT 1")
    MetricDefinition selectByMetricName(@Param("metricName") String metricName);

    /**
     * 检查指标编码是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_metric_definition WHERE metric_code = #{metricCode} AND deleted = 0")
    int existsByMetricCode(@Param("metricCode") String metricCode);

    /**
     * 根据指标类型查询
     */
    @Select("SELECT * FROM edams_metric_definition WHERE metric_type = #{metricType} AND status = 1 AND deleted = 0 ORDER BY created_time DESC")
    List<MetricDefinition> selectByMetricType(@Param("metricType") String metricType);

    /**
     * 根据业务域查询
     */
    @Select("SELECT * FROM edams_metric_definition WHERE domain = #{domain} AND status = 1 AND deleted = 0 ORDER BY created_time DESC")
    List<MetricDefinition> selectByDomain(@Param("domain") String domain);

    /**
     * 根据主题查询
     */
    @Select("SELECT * FROM edams_metric_definition WHERE subject = #{subject} AND status = 1 AND deleted = 0 ORDER BY created_time DESC")
    List<MetricDefinition> selectBySubject(@Param("subject") String subject);

    /**
     * 根据状态查询
     */
    @Select("SELECT * FROM edams_metric_definition WHERE status = #{status} AND deleted = 0 ORDER BY created_time DESC")
    List<MetricDefinition> selectByStatus(@Param("status") Integer status);

    /**
     * 更新指标状态
     */
    @Update("UPDATE edams_metric_definition SET status = #{status}, updated_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新当前值
     */
    @Update("UPDATE edams_metric_definition SET current_value = #{currentValue}, updated_time = NOW() WHERE id = #{id}")
    int updateCurrentValue(@Param("id") Long id, @Param("currentValue") BigDecimal currentValue);

    /**
     * 增加版本号
     */
    @Update("UPDATE edams_metric_definition SET version = version + 1, updated_time = NOW() WHERE id = #{id}")
    int incrementVersion(@Param("id") Long id);

    /**
     * 根据数据源表查询
     */
    @Select("SELECT * FROM edams_metric_definition WHERE source_table = #{sourceTable} AND deleted = 0 ORDER BY created_time DESC")
    List<MetricDefinition> selectBySourceTable(@Param("sourceTable") String sourceTable);

    /**
     * 查询热门指标
     */
    @Select("SELECT * FROM edams_metric_definition WHERE status = 1 AND deleted = 0 ORDER BY created_time DESC LIMIT #{limit}")
    List<MetricDefinition> selectTopMetrics(@Param("limit") Integer limit);
}
