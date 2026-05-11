package com.enterprise.dataplatform.masking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaskingResponse {

    private Boolean success;
    private List<Map<String, Object>> maskedData;
    private List<String> maskedColumns;
    private Integer recordCount;
    private String message;
    private String errorCode;
}
