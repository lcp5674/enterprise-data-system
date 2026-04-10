package com.enterprise.edams.watermark.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchWatermarkRequest {
    
    private List<AddWatermarkRequest> requests;
}
