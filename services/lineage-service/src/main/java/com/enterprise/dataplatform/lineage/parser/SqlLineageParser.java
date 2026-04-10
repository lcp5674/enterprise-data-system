package com.enterprise.dataplatform.lineage.parser;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * SQL/DDL Lineage Parser
 * Extracts data lineage from SQL statements
 */
@Slf4j
@Component
public class SqlLineageParser {

    /**
     * Parse SQL and extract lineage relations
     */
    public LineageParseResult parse(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            
            if (statement instanceof Insert) {
                return parseInsert((Insert) statement);
            } else if (statement instanceof Select) {
                return parseSelect((Select) statement);
            } else if (statement instanceof Update) {
                return parseUpdate((Update) statement);
            } else if (statement instanceof Delete) {
                return parseDelete((Delete) statement);
            }
            
            return LineageParseResult.builder()
                    .success(false)
                    .error("Unsupported SQL statement type")
                    .build();
                    
        } catch (JSQLParserException e) {
            log.error("Failed to parse SQL: {}", sql, e);
            return LineageParseResult.builder()
                    .success(false)
                    .error("SQL parse error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Parse INSERT statement
     */
    private LineageParseResult parseInsert(Insert insert) {
        List<ColumnLineage> sourceColumns = extractSourceTables(insert.getSelect());
        List<ColumnLineage> targetColumns = extractTargetColumns(insert.getTable().getName());
        
        return LineageParseResult.builder()
                .success(true)
                .sourceTables(Collections.singletonList(insert.getTable().getName()))
                .targetTable(insert.getTable().getName())
                .columnLineages(buildColumnMappings(sourceColumns, targetColumns))
                .sqlType("INSERT")
                .build();
    }

    /**
     * Parse SELECT statement (CTAS)
     */
    private LineageParseResult parseSelect(Select select) {
        List<String> sourceTables = extractSourceTablesFromSelect(select);
        
        return LineageParseResult.builder()
                .success(true)
                .sourceTables(sourceTables)
                .sqlType("SELECT")
                .build();
    }

    /**
     * Parse UPDATE statement
     */
    private LineageParseResult parseUpdate(Update update) {
        return LineageParseResult.builder()
                .success(true)
                .sourceTables(Collections.singletonList(update.getTable().getName()))
                .targetTable(update.getTable().getName())
                .sqlType("UPDATE")
                .build();
    }

    /**
     * Parse DELETE statement
     */
    private LineageParseResult parseDelete(Delete delete) {
        return LineageParseResult.builder()
                .success(true)
                .sourceTables(Collections.singletonList(delete.getTable().getName()))
                .sqlType("DELETE")
                .build();
    }

    /**
     * Extract source tables from SELECT
     */
    private List<String> extractSourceTablesFromSelect(Select select) {
        List<String> tables = new ArrayList<>();
        
        select.getSelectBody().accept(new net.sf.jsqlparser.statement.select.PlainSelectVisitor() {
            @Override
            public void visit(net.sf.jsqlparser.statement.select.PlainSelect plainSelect) {
                plainSelect.getFromItem().accept(new net.sf.jsqlparser.schema.TableVisitor() {
                    @Override
                    public void visit(net.sf.jsqlparser.schema.Table table) {
                        tables.add(table.getName());
                    }
                });
                
                if (plainSelect.getJoins() != null) {
                    for (var join : plainSelect.getJoins()) {
                        join.getRightItem().accept(new net.sf.jsqlparser.schema.TableVisitor() {
                            @Override
                            public void visit(net.sf.jsqlparser.schema.Table table) {
                                tables.add(table.getName());
                            }
                        });
                    }
                }
            }
            
            @Override
            public void visit(net.sf.jsqlparser.statement.select.SetOperationList setOperationList) {}
            @Override
            public void visit(net.sf.jsqlparser.statement.select.ParenthesedSelect plainSelect) {}
        });
        
        return tables;
    }

    /**
     * Extract source tables from SELECT statement
     */
    private List<ColumnLineage> extractSourceTables(Select select) {
        List<ColumnLineage> columns = new ArrayList<>();
        extractSourceTablesFromSelect(select).forEach(table -> {
            columns.add(ColumnLineage.builder()
                    .tableName(table)
                    .columns(Collections.emptyList())
                    .build());
        });
        return columns;
    }

    /**
     * Extract target columns
     */
    private List<ColumnLineage> extractTargetColumns(String tableName) {
        return Collections.singletonList(ColumnLineage.builder()
                .tableName(tableName)
                .columns(Collections.emptyList())
                .build());
    }

    /**
     * Build column mappings
     */
    private List<ColumnMapping> buildColumnMappings(
            List<ColumnLineage> sources, List<ColumnLineage> targets) {
        
        List<ColumnMapping> mappings = new ArrayList<>();
        
        if (sources.isEmpty() || targets.isEmpty()) {
            return mappings;
        }

        // Simple 1:1 column mapping (in real implementation, would parse expressions)
        for (ColumnLineage source : sources) {
            for (ColumnLineage target : targets) {
                mappings.add(ColumnMapping.builder()
                        .sourceTable(source.getTableName())
                        .sourceColumn(null) // Would be extracted from SELECT columns
                        .targetTable(target.getTableName())
                        .targetColumn(null)
                        .expression(null)
                        .build());
            }
        }
        
        return mappings;
    }

    /**
     * Parse DDL statements (CREATE TABLE, ALTER TABLE)
     */
    public DDLParseResult parseDDL(String ddl) {
        try {
            // For DDL parsing, we can detect table references
            String upperDDL = ddl.toUpperCase();
            
            if (upperDDL.contains("CREATE TABLE")) {
                return parseCreateTable(ddl);
            }
            
            return DDLParseResult.builder()
                    .success(false)
                    .error("Unsupported DDL statement")
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to parse DDL: {}", ddl, e);
            return DDLParseResult.builder()
                    .success(false)
                    .error("DDL parse error: " + e.getMessage())
                    .build();
        }
    }

    private DDLParseResult parseCreateTable(String ddl) {
        // Extract table name
        String tableName = extractTableName(ddl);
        
        return DDLParseResult.builder()
                .success(true)
                .tableName(tableName)
                .ddlType("CREATE_TABLE")
                .build();
    }

    private String extractTableName(String ddl) {
        int createIndex = ddl.toUpperCase().indexOf("CREATE TABLE");
        if (createIndex == -1) {
            createIndex = ddl.toUpperCase().indexOf("CREATE TABLE IF NOT EXISTS");
        }
        
        if (createIndex >= 0) {
            int start = ddl.indexOf("(", createIndex);
            int end = ddl.indexOf(" ", createIndex + 13);
            if (end > createIndex && (start == -1 || end < start)) {
                return ddl.substring(end, start).trim().replace("`", "").replace("\"", "");
            }
        }
        
        return null;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LineageParseResult {
        private boolean success;
        private String error;
        private String sqlType;
        private List<String> sourceTables;
        private String targetTable;
        private List<ColumnLineage> columnLineages;
        private List<ColumnMapping> columnMappings;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DDLParseResult {
        private boolean success;
        private String error;
        private String ddlType;
        private String tableName;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ColumnLineage {
        private String tableName;
        private List<String> columns;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ColumnMapping {
        private String sourceTable;
        private String sourceColumn;
        private String targetTable;
        private String targetColumn;
        private String expression;
    }
}
