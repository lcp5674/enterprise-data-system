package com.enterprise.edams.sandbox.dto;

import com.enterprise.edams.sandbox.entity.SampleType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SampleDataRequestDto {
    
    private Long assetId;
    private String assetName;
    private Long userId;
    private String userName;
    private SampleType sampleType;
    private Integer sampleCount;
    private BigDecimal sizeLimit;
    private Long desensitizationRuleId;
    private String purpose;
    private String remark;
}
