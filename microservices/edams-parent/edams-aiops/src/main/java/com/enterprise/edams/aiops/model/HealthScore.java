package com.enterprise.edams.aiops.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 健康评分实体
 * 
 * 存储系统/服务的综合健康评分及趋势分析
 *
 * @author AIOps Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("aiops_health_score")
public class HealthScore {

    /**
     * 评分记录ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 评估对象类型: SYSTEM, SERVICE, COMPONENT
     */
    private String objectType;

    /**
     * 评估对象名称
     */
    private String objectName;

    /**
     * 父级服务/系统名称
     */
    private String parentName;

    /**
     * 综合健康评分 (0-100)
     */
    private Integer overallScore;

    /**
     * 可用性评分
     */
    private Integer availabilityScore;

    /**
     * 性能评分
     */
    private Integer performanceScore;

    /**
     * 资源利用率评分
     */
    private Integer resourceScore;

    /**
     * 错误率评分
     */
    private Integer errorRateScore;

    /**
     * 安全评分
     */
    private Integer securityScore;

    /**
     * 各维度评分详情 (JSON格式)
     */
    private String dimensionDetails;

    /**
     * 风险等级: LOW, MEDIUM, HIGH, CRITICAL
     */
    private String riskLevel;

    /**
     * 识别出的风险点列表 (JSON格式)
     */
    private String riskPoints;

    /**
     * 建议措施列表 (JSON格式)
     */
    private String recommendations;

    /**
     * 趋势: IMPROVING, STABLE, DEGRADING
     */
    private String trend;

    /**
     * 与上一周期相比的变化
     */
    private Integer scoreChange;

    /**
     * 评分时间
     */
    private LocalDateTime evaluatedAt;

    /**
     * 下一个评估时间
     */
    private LocalDateTime nextEvaluationAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
