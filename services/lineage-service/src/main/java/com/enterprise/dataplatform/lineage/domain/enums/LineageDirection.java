package com.enterprise.dataplatform.lineage.domain.enums;

/**
 * Lineage query direction
 */
public enum LineageDirection {
    /**
     * Upstream (data sources)
     */
    UPSTREAM,
    
    /**
     * Downstream (data targets)
     */
    DOWNSTREAM,
    
    /**
     * Both directions
     */
    BOTH
}
