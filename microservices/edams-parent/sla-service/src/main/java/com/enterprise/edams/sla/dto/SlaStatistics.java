package com.enterprise.edams.sla.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlaStatistics {
    
    private long totalSlas;
    private long activeSlas;
    private long breachedSlas;
    private long totalViolations;
    private double complianceRate;
}
