package com.enterprise.edams.analysis.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableAnalysisResult {

    private String tableDescription;
    private String tableAlias;
    private String tableCategory;
    private Map<String, String> fieldDescriptions;
    private String primaryKey;
    private String[] importantForeignKeys;
    private BigDecimal confidence;
}
