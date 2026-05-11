package com.enterprise.edams.analysis.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestResponse {

    private boolean success;
    private String message;
    private Long responseTimeMs;
    private String modelName;
    private String modelVersion;
    private Integer contextWindow;
}
