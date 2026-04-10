package com.enterprise.edams.incentive.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SignInResult {
    
    private boolean success;
    private String message;
    private BigDecimal points;
    private BigDecimal bonusPoints;
    private Integer totalDays;
    private BigDecimal balance;
}
