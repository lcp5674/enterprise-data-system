package com.enterprise.dataplatform.metadata.dto.response;

import com.enterprise.dataplatform.metadata.entity.MetadataObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for metadata object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataResponse {

    private Long id;
    private String objectId;
    private String objectType;
    private String domainCode;
    private String name;
    private String displayName;
    private String description;
    private String schemaInfo;
    private String tags;
    private String owner;
    private String ownerEmail;
    private String sensitivity;
    private String status;
    private String dataSource;
    private String locationPath;
    private Long rowCount;
    private Long sizeBytes;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MetadataFieldInfo> fields;

    /**
     * Inner class for field information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetadataFieldInfo {
        private Long id;
        private String fieldName;
        private String fieldType;
        private Integer fieldLength;
        private Boolean nullable;
        private Boolean primaryKey;
        private String description;
        private String sampleValues;
        private String sensitivityLevel;
        private String businessComment;
        private Integer ordinalPosition;
    }

    /**
     * Convert entity to response DTO
     */
    public static MetadataResponse fromEntity(MetadataObject entity) {
        return MetadataResponse.builder()
                .id(entity.getId())
                .objectId(entity.getObjectId())
                .objectType(entity.getObjectType())
                .domainCode(entity.getDomainCode())
                .name(entity.getName())
                .displayName(entity.getDisplayName())
                .description(entity.getDescription())
                .schemaInfo(entity.getSchemaInfo())
                .tags(entity.getTags())
                .owner(entity.getOwner())
                .ownerEmail(entity.getOwnerEmail())
                .sensitivity(entity.getSensitivity())
                .status(entity.getStatus())
                .dataSource(entity.getDataSource())
                .locationPath(entity.getLocationPath())
                .rowCount(entity.getRowCount())
                .sizeBytes(entity.getSizeBytes())
                .lastUpdated(entity.getLastUpdated())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
