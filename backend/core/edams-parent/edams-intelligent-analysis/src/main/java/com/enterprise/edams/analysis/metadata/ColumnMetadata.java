package com.enterprise.edams.analysis.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMetadata {

    private String columnName;
    private String dataType;
    private String columnType;
    private String columnComment;
    private Boolean nullable;
    private Boolean primaryKey;
    private Boolean foreignKey;
    private String defaultValue;
    private Integer ordinalPosition;
    private Integer characterMaximumLength;
    private Integer numericPrecision;
    private Integer numericScale;
}
