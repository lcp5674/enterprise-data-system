package com.enterprise.edams.llm.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.llm.entity.LlmCostRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成本记录Mapper
 */
@Mapper
public interface LlmCostRecordMapper extends BaseMapper<LlmCostRecord> {

    /**
     * 查询用户的成本趋势
     */
    @Select("SELECT * FROM llm_cost_record WHERE user_id = #{userId} AND stat_date BETWEEN #{startDate} AND #{endDate} ORDER BY stat_date")
    List<LlmCostRecord> selectUserCostTrend(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 查询租户的总成本
     */
    @Select("SELECT COALESCE(SUM(total_cost), 0) FROM llm_cost_record WHERE tenant_id = #{tenantId}")
    BigDecimal sumTenantTotalCost(@Param("tenantId") Long tenantId);

    /**
     * 分页查询成本记录
     */
    @Select("SELECT * FROM llm_cost_record WHERE tenant_id = #{tenantId} ORDER BY stat_date DESC")
    IPage<LlmCostRecord> selectPageByTenant(Page<LlmCostRecord> page, @Param("tenantId") Long tenantId);

    /**
     * 查询模型的成本排名
     */
    @Select("SELECT model_code, SUM(total_cost) as cost FROM llm_cost_record " +
           "WHERE tenant_id = #{tenantId} AND stat_date BETWEEN #{startDate} AND #{endDate} " +
           "GROUP BY model_code ORDER BY cost DESC")
    List<LlmCostRecord> selectModelCostRanking(@Param("tenantId") Long tenantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
