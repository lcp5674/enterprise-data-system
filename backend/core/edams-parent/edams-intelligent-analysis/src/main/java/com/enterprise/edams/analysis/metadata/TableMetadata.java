package com.enterprise.edams.analysis.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableMetadata {

    private String schemaName;
    private String tableName;
    private String tableComment;
    private String tableType;
    private Long estimatedRowCount;
    private Long dataSizeBytes;
    private List<ColumnMetadata> columns;

    public String getFullTableName() {
        return schemaName != null ? schemaName + "." + tableName : tableName;
    }
}
