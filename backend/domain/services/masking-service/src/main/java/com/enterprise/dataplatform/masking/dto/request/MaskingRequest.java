package com.enterprise.dataplatform.masking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaskingRequest {

    @NotEmpty(message = "资产ID不能为空")
    private String assetId;

    private String assetName;

    private List<String> columns;

    @NotEmpty(message = "数据不能为空")
    private List<Map<String, Object>> data;

    private String classificationLevel;

    private Boolean preserveOriginalFormat;
}
