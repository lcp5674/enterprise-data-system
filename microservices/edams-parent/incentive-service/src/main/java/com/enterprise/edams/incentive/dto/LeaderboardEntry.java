package com.enterprise.edams.incentive.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LeaderboardEntry {
    
    private int rank;
    private Long userId;
    private String userName;
    private BigDecimal points;
}
