package com.enterprise.dataplatform.lineage.dto.request;

import com.enterprise.dataplatform.lineage.domain.enums.LineageType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating lineage relationship
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLineageRequest {

    @NotBlank(message = "Source asset ID is required")
    private String sourceAssetId;

    private String sourceFieldId;

    @NotBlank(message = "Target asset ID is required")
    private String targetAssetId;

    private String targetFieldId;

    private LineageType lineageType;

    private String transformDesc;

    private String transformSql;

    private String taskName;

    private String jobId;

    private Double confidence;
}
