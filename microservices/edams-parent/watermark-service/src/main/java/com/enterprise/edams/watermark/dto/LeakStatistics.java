package com.enterprise.edams.watermark.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeakStatistics {
    
    private long totalCases;
    private long investigating;
    private long confirmed;
    private long disposed;
    private double resolutionRate;
}
