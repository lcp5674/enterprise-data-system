package com.enterprise.edams.watermark.dto;

import com.enterprise.edams.watermark.entity.LeakType;
import com.enterprise.edams.watermark.entity.TraceStatus;
import lombok.Data;

@Data
public class LeakTraceSearchRequest {
    
    private Long suspectUserId;
    private LeakType leakType;
    private TraceStatus status;
    private int pageNum = 1;
    private int pageSize = 20;
}
