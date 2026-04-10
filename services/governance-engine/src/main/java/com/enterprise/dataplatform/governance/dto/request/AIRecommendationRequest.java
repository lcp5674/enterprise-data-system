package com.enterprise.dataplatform.governance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendationRequest {

    @NotBlank(message = "推荐类型不能为空")
    private String recommendationType;

    private String context;

    private String targetAssetId;

    private String domain;

    private List<String> considerationFactors;

    private Integer limitResults = 5;
}
