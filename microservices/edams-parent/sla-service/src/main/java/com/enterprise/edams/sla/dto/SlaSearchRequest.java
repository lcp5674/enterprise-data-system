package com.enterprise.edams.sla.dto;

import com.enterprise.edams.sla.entity.*;
import lombok.Data;

@Data
public class SlaSearchRequest {
    
    private String slaCode;
    private String slaName;
    private Long assetId;
    private SlaType slaType;
    private SlaStatus status;
    private SlaSeverity severity;
    private int pageNum = 1;
    private int pageSize = 20;
}
