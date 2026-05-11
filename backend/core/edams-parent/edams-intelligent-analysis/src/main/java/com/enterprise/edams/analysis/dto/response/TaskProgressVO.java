package com.enterprise.edams.analysis.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgressVO {

    private Long taskId;
    private String taskCode;
    private String taskName;
    private Integer progress;
    private Integer totalTables;
    private Integer analyzedTables;
    private Integer totalBatches;
    private Integer completedBatches;
    private Integer successCount;
    private Integer failureCount;
    private String status;
    private String estimatedTimeRemaining;
    private Double tablesPerMinute;
}
