package com.enterprise.dataplatform.metadata.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for metadata field
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataFieldRequest {

    @NotBlank(message = "Field name is required")
    private String fieldName;

    @NotBlank(message = "Field type is required")
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
