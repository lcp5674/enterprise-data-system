package com.enterprise.edams.analysis.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageRelation {

    private String sourceTable;
    private String sourceField;
    private String targetTable;
    private String targetField;
    private LineageType lineageType;
    private String transformation;
    private BigDecimal confidence;

    public enum LineageType {
        DIRECT,
        AGGREGATED,
        TRANSFORMED
    }
}
