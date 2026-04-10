package com.enterprise.edams.value.dto;

import com.enterprise.edams.value.entity.AssessmentStatus;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 评估搜索请求
 */
@Data
public class AssessmentSearchRequest {
    
    private Long assetId;
    
    private BigDecimal minScore;
    
    private BigDecimal maxScore;
    
    private AssessmentStatus status;
    
    private int pageNum = 1;
    
    private int pageSize = 20;
}
