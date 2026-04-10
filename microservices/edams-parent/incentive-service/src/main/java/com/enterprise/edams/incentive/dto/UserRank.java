package com.enterprise.edams.incentive.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserRank {
    
    private Long userId;
    private String userName;
    private Integer rank;
    private BigDecimal points;
    private int totalParticipants;
    private Integer level;
}
