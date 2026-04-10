package com.enterprise.edams.sla.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SlaCompliance {
    
    private Long slaId;
    private String slaCode;
    private BigDecimal complianceRate;
    private int totalChecks;
    private int passedChecks;
    private int failedChecks;
}
