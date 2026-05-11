package com.enterprise.edams.analysis.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorDefinition {

    private String indicatorName;
    private String indicatorCode;
    private IndicatorType indicatorType;
    private String description;
    private String formula;
    private String unit;
    private List<String> dimensions;
    private AggregationType aggregationType;
    private String dataType;
    private BigDecimal confidence;

    public enum IndicatorType {
        ATOMIC,
        DERIVED,
        CALCULATED
    }

    public enum AggregationType {
        SUM,
        COUNT,
        AVG,
        MAX,
        MIN,
        DISTINCT,
        COUNT_DISTINCT
    }
}
