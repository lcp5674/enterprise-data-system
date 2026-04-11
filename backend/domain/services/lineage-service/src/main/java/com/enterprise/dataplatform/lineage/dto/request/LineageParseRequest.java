package com.enterprise.dataplatform.lineage.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for parsing DDL/SQL to extract lineage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageParseRequest {

    @NotBlank(message = "SQL/DDL content is required")
    private String sqlContent;

    @Builder.Default
    private String parseType = "DDL";

    private String datasourceId;
}
