package com.enterprise.dataplatform.metadata.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for metadata search
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataSearchRequest {

    private String keyword;

    private String objectType;

    private String domainCode;

    private String sensitivity;

    private String status;

    private String owner;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;
}
