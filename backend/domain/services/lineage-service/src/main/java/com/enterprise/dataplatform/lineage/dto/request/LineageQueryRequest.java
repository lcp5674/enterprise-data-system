package com.enterprise.dataplatform.lineage.dto.request;

import com.enterprise.dataplatform.lineage.domain.enums.LineageDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for lineage query
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageQueryRequest {

    private String assetId;

    private LineageDirection direction;

    @Builder.Default
    private Integer depth = 3;

    private Boolean includeTasks;

    private String layout;
}
