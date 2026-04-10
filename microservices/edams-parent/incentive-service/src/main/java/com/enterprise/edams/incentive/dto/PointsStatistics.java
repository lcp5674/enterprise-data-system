package com.enterprise.edams.incentive.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PointsStatistics {
    
    private long totalUsers;
    private BigDecimal totalPoints;
    private BigDecimal averagePoints;
}
