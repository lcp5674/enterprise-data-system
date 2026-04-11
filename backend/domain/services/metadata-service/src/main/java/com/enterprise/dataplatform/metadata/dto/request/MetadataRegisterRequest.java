package com.enterprise.dataplatform.metadata.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for metadata registration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataRegisterRequest {

    @NotBlank(message = "Object ID is required")
    private String objectId;

    @NotBlank(message = "Object type is required")
    private String objectType;

    @NotBlank(message = "Domain code is required")
    private String domainCode;

    @NotBlank(message = "Name is required")
    private String name;

    private String displayName;

    private String description;

    private String schemaInfo;

    private String tags;

    private String owner;

    private String ownerEmail;

    private String sensitivity;

    private String dataSource;

    private String locationPath;

    private Long rowCount;

    private Long sizeBytes;
}
