package com.enterprise.edams.sandbox.dto;

import com.enterprise.edams.sandbox.entity.SampleRequestStatus;
import lombok.Data;

@Data
public class SampleSearchRequest {
    
    private Long userId;
    private Long assetId;
    private SampleRequestStatus status;
    private int pageNum = 1;
    private int pageSize = 20;
}
