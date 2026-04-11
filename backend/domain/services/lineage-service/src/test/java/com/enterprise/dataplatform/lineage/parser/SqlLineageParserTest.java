package com.enterprise.dataplatform.lineage.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SqlLineageParserTest {

    private SqlLineageParser parser;

    @BeforeEach
    void setUp() {
        parser = new SqlLineageParser();
    }

    @Test
    void parseInsertStatement_shouldExtractLineage() {
        String sql = "INSERT INTO target_table SELECT id, name FROM source_table";

        Map<String, Object> result = parser.parse(sql);

        assertNotNull(result);
        assertTrue(result.containsKey("sources"));
        assertTrue(result.containsKey("target"));
        assertEquals("INSERT", result.get("type"));
    }

    @Test
    void parseUpdateStatement_shouldExtractLineage() {
        String sql = "UPDATE target_table SET name = 'test' WHERE id = 1";

        Map<String, Object> result = parser.parse(sql);

        assertNotNull(result);
        assertEquals("UPDATE", result.get("type"));
    }

    @Test
    void parseDeleteStatement_shouldExtractLineage() {
        String sql = "DELETE FROM target_table WHERE id = 1";

        Map<String, Object> result = parser.parse(sql);

        assertNotNull(result);
        assertEquals("DELETE", result.get("type"));
    }

    @Test
    void parseCTASStatement_shouldExtractLineage() {
        String sql = "CREATE TABLE new_table AS SELECT * FROM source_table";

        Map<String, Object> result = parser.parse(sql);

        assertNotNull(result);
        assertEquals("CTAS", result.get("type"));
    }

    @Test
    void parseSelectStatement_shouldExtractLineage() {
        String sql = "SELECT a.id, b.name FROM table_a a JOIN table_b b ON a.id = b.id";

        Map<String, Object> result = parser.parse(sql);

        assertNotNull(result);
        assertEquals("SELECT", result.get("type"));
    }

    @Test
    void parseComplexQuery_shouldExtractAllTables() {
        String sql = """
            INSERT INTO final_table
            SELECT a.id, b.name, c.value
            FROM table_a a
            JOIN table_b b ON a.id = b.id
            LEFT JOIN table_c c ON b.id = c.id
            WHERE a.status = 'active'
            """;

        Map<String, Object> result = parser.parse(sql);

        assertNotNull(result);
        assertTrue(result.containsKey("sources"));
    }

    @Test
    void parseCreateTableDDL_shouldExtractColumns() {
        String ddl = "CREATE TABLE test_table (id INT, name VARCHAR(100), email VARCHAR(255))";

        Map<String, Object> result = parser.parse(ddl);

        assertNotNull(result);
        assertEquals("CREATE_TABLE", result.get("type"));
        assertTrue(result.containsKey("columns"));
    }

    @Test
    void parseAlterTableDDL_shouldExtractChanges() {
        String ddl = "ALTER TABLE test_table ADD COLUMN new_col VARCHAR(50)";

        Map<String, Object> result = parser.parse(ddl);

        assertNotNull(result);
        assertEquals("ALTER_TABLE", result.get("type"));
    }

    @Test
    void extractColumnLineage_shouldReturnColumnMappings() {
        String sql = "INSERT INTO target SELECT a.col1, b.col2 FROM source_a a, source_b b";

        List<Map<String, String>> columnLineage = parser.extractColumnLineage(sql);

        assertNotNull(columnLineage);
    }

    @Test
    void detectLineageType_shouldReturnCorrectType() {
        assertEquals("DIRECT", parser.detectLineageType("INSERT INTO t SELECT * FROM s"));
        assertEquals("INDIRECT", parser.detectLineageType("INSERT INTO t SELECT a+b FROM s"));
        assertEquals("TRANSFORM", parser.detectLineageType("INSERT INTO t SELECT UPPER(name) FROM s"));
    }

    @Test
    void extractTableNames_shouldReturnAllTables() {
        String sql = "SELECT * FROM t1 JOIN t2 ON t1.id = t2.id LEFT JOIN t3 ON t2.id = t3.id";

        Set<String> tables = parser.extractTableNames(sql);

        assertNotNull(tables);
        assertTrue(tables.contains("t1"));
        assertTrue(tables.contains("t2"));
        assertTrue(tables.contains("t3"));
    }

    @Test
    void extractColumnNames_shouldReturnAllColumns() {
        String sql = "SELECT id, name, email FROM users WHERE status = 'active'";

        Set<String> columns = parser.extractColumnNames(sql);

        assertNotNull(columns);
        assertTrue(columns.contains("id"));
        assertTrue(columns.contains("name"));
        assertTrue(columns.contains("email"));
    }

    @Test
    void calculateConfidence_shouldReturnHighForSimpleSelect() {
        String sql = "INSERT INTO t SELECT * FROM s";

        double confidence = parser.calculateConfidence(sql);

        assertEquals(1.0, confidence, 0.01);
    }

    @Test
    void calculateConfidence_shouldReturnLowerForComplexTransform() {
        String sql = "INSERT INTO t SELECT UPPER(name) || LOWER(email) FROM s";

        double confidence = parser.calculateConfidence(sql);

        assertTrue(confidence < 1.0);
        assertTrue(confidence > 0.5);
    }

    @Test
    void isValidSql_shouldReturnTrueForValidSQL() {
        assertTrue(parser.isValidSql("SELECT * FROM table"));
        assertTrue(parser.isValidSql("INSERT INTO t SELECT * FROM s"));
    }

    @Test
    void isValidSql_shouldReturnFalseForInvalidSQL() {
        assertFalse(parser.isValidSql("SELECT *"));
        assertFalse(parser.isValidSql(""));
    }
}
