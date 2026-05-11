package com.enterprise.edams.analysis.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskQueryDTO {

    private String keyword;

    private String status;

    private String executionMode;

    private Long datasourceId;

    private String executor;

    private Integer page = 0;

    private Integer size = 20;
}
