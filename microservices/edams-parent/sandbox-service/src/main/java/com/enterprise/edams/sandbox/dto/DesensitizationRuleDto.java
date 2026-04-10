package com.enterprise.edams.sandbox.dto;

import com.enterprise.edams.sandbox.entity.DataType;
import com.enterprise.edams.sandbox.entity.DesensitizationMethod;
import lombok.Data;

@Data
public class DesensitizationRuleDto {
    
    private String ruleName;
    private DataType dataType;
    private DesensitizationMethod method;
    private String params;
    private Integer priority;
    private String description;
}
