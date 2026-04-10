package com.enterprise.edams.sla.dto;

import com.enterprise.edams.sla.entity.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SlaUpdateRequest {
    
    private String slaName;
    private BigDecimal targetValue;
    private String unit;
    private Integer windowSize;
    private SlaSeverity severity;
    private String contact;
    private String alertMethods;
    private String description;
}
