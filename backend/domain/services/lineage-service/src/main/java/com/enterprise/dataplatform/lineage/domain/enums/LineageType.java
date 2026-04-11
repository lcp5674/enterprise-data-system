package com.enterprise.dataplatform.lineage.domain.enums;

/**
 * Lineage relationship types
 */
public enum LineageType {
    /**
     * ETL process lineage
     */
    ETL,
    
    /**
     * SQL-based lineage
     */
    SQL,
    
    /**
     * Manual lineage
     */
    MANUAL,
    
    /**
     * API-based lineage
     */
    API,
    
    /**
     * Streaming data lineage
     */
    STREAM
}
